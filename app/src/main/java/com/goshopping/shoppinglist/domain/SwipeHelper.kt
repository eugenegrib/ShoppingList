package com.goshopping.shoppinglist.domain

import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.*
import android.view.ActionMode
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*


@SuppressLint("ClickableViewAccessibility")
abstract class SwipeHelper(private val recyclerView: RecyclerView) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var buttons: MutableList<UnderlayButton>
    private var swipedPos = -1//
    private var swipeThreshold = 0.5f
    private var buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>> =
        mutableMapOf()//список сохраненных в памяти элементов
    private lateinit var recoverQueue: LinkedList<Int>//цепь элементов


    private val onTouchListener = View.OnTouchListener { _, e ->
        if (swipedPos < 0) return@OnTouchListener false
        buttonsBuffer[swipedPos]?.forEach { it.handle(e) }//каждому элементу присвоить кнопку
        recoverQueue.add(swipedPos)//добавить в цепь элемент, к которому мы прикоснулись
        swipedPos = -1
        //recoverSwipedItem()
        true
    }

    init {
        buttons = ArrayList()
        //gestureDetector = GestureDetector(context, gestureListener)
        recyclerView.setOnTouchListener(onTouchListener)
        buttonsBuffer = HashMap()
        recoverQueue = object : LinkedList<Int>() {
            override fun add(element: Int): Boolean {
                return if (contains(element)) false else super.add(element)
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        //если позиция, до которой мы дотронулись не равна элементу, который мы смахиваем
        if (swipedPos != viewHolder.layoutPosition) {
            recoverQueue.poll()
            recoverQueue.add(swipedPos)
            //присвоить swipedPos идентификатор смахиваемого элемента
            swipedPos = viewHolder.layoutPosition
        }


        //если в сохраненном списке элементов есть смахиваемый, то...
        if (buttonsBuffer.containsKey(swipedPos)) {
            //присвоить его buttons
            buttons = buttonsBuffer[swipedPos]!!
        } else {
            //иначе очистить buttons
            buttons.clear()
        }

        buttonsBuffer.clear()//очистить сохраненный список
        swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH
        recoverSwipedItem()
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val position = recoverQueue.poll() ?: return
            recyclerView.adapter?.notifyItemChanged(position)
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val position = viewHolder.layoutPosition
        val itemView = viewHolder.itemView
        if (position < 0) {
            swipedPos = position
            return
        }
        var translationX = dX

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            var buffer: MutableList<UnderlayButton> = mutableListOf()
            if (dX < 0) {
                if (!buttonsBuffer.containsKey(position)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    recoverSwipedItem()
                    buttonsBuffer[position] = buffer
                } else {
                    buffer = buttonsBuffer[position]!!
                }
                translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
                drawButtons(canvas, itemView, buffer, position, translationX)
            }
        }
        super.onChildDraw(
            canvas,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }


    private fun drawButtons(
        canvas: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        position: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size

        for (button in buffer) {

            val left = right - dButtonWidth
            button.onDraw(
                canvas,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                position
            )
            right = left
        }
    }


    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder?,
        underlayButtons: MutableList<UnderlayButton>?
    )

    class UnderlayButton(
        private val text: String,
        private val color: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null


        fun handle(event: MotionEvent) {
            clickRegion?.let {
                if (it.contains(event.x, event.y)) {
                    clickListener.onClick(pos)
                }
            }
        }

        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Draw background
            p.color = color
            c.drawRect(rect, p)

            // Draw Text
            p.color = Color.WHITE
            p.textSize = 32f
            val r = Rect()
            val cHeight = rect.height()
            val cWidth = rect.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text, 0, text.length, r)
            val x = cWidth / 2f - r.width() / 2f - r.left
            val y = cHeight / 2f + r.height() / 2f - r.bottom
            c.drawText(text, rect.left + x, rect.top + y, p)
            clickRegion = rect
            this.pos = pos
        }
    }

    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    companion object {
        const val BUTTON_WIDTH = 200
    }


}




