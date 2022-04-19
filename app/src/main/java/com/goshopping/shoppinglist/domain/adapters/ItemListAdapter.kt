package com.goshopping.shoppinglist.domain.adapters

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.databinding.ItemRecyclerViewBinding
import com.goshopping.shoppinglist.domain.MoreFunctions
import java.util.*


/**
 * [ListAdapter] implementation for the recyclerview.
 */

class ItemListAdapter(
    private val moreFunctions: MoreFunctions,
    private val onItemClicked: (Item) -> Unit
) : ListAdapter<Item, ItemListAdapter.ViewHolder>(TaskItemDiffCallback()) {

    var onMoved: (() -> Unit)? = null

    fun onMoved() {
        onMoved?.invoke()
    }


    fun swap(targetPos: Int, startsPos: Int) {
        val mutableList = currentList.toMutableList()
        Collections.swap(mutableList, startsPos, targetPos)
        submitList(mutableList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createItem(parent)
    }

    // Когда элемент становится видимым пользователю
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        // После добавление нового элемента вызываем код ниже
        if (moreFunctions.isAddedNewTask) {
            val editText = holder.view.findViewById<EditText>(R.id.editText)
            editText.isFocusableInTouchMode = true
            editText.setText("")
            editText.requestFocus()
            moreFunctions.showKeyboard(editText)
            moreFunctions.isAddedNewTask = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(holder.layoutPosition)
        val binding = ItemRecyclerViewBinding.bind(holder.view)

        with(binding) {
            editText.setText(current.itemName)
            checkBoxDone.isChecked = current.itemCheck

            checkBoxDone.setOnClickListener {
                editText.clearFocus()
                onItemClicked(current.copy(itemCheck = checkBoxDone.isChecked,itemName = binding.editText.text.toString()))
                moreFunctions.clearFocus()
                moreFunctions.closeKeyboard(editText)
                moreFunctions.updateCheckBox(current)
            }

            ibDelete.setOnClickListener {
                moreFunctions.deleteItem(current)
                moreFunctions.clearFocus()
                onMoved?.invoke()
            }

            editText.setOnClickListener {
                if (!editText.hasFocus()) {
                    editText.requestFocus()
                }
            }

            btnMoveItem.setOnTouchListener { _, _ ->
                moreFunctions.closeKeyboard(editText)
                moreFunctions.onStartDrag(holder)
                moreFunctions.clearFocus()
                true
            }



            editText.onFocusChangeListener = View.OnFocusChangeListener { _, p1 ->
                if (p1) {
                    ibDelete.visibility = View.VISIBLE
                } else {
                    ibDelete.visibility = View.INVISIBLE
                    moreFunctions.updateItem(getItem(holder.layoutPosition).copy(itemName = binding.editText.text.toString()))
                    Log.v(TAG, "Сохранение в базу данных")
                }
            }
        }
    }


    companion object {
        fun createItem(parent: ViewGroup): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recycler_view, parent, false)
            )
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

class TaskItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}