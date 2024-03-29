package com.goshopping.shoppinglist.domain.adapters

import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.databinding.MarkedItemRecyclerViewBinding
import com.goshopping.shoppinglist.domain.MoreFunctions
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


/**
 * [ListAdapter] implementation for the recyclerview.
 */

class MarkedItemListAdapter(
    private val moreFunctions: MoreFunctions
) : ListAdapter<Item, MarkedItemListAdapter.MarkedViewHolder>(MarkedTaskItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkedViewHolder {
        return createItem(parent)
    }

    override fun onBindViewHolder(holder: MarkedViewHolder, position: Int) {
        val current = getItem(holder.bindingAdapterPosition)
        with(MarkedItemRecyclerViewBinding.bind(holder.view)) {
            val string = SpannableString(current.itemName)
            string.setSpan(StrikethroughSpan(), 0, string.length, 0)
            editText.text = string
            checkBoxDone.isChecked = true
            checkBoxDone.setOnClickListener {
                moreFunctions.updateCheckBox(false,current.copy(itemCheck = checkBoxDone.isChecked))
            }
            ibDelete.setOnClickListener {
                moreFunctions.deleteItem(getItem(holder.bindingAdapterPosition), true)
            }
            editText.onFocusChangeListener = View.OnFocusChangeListener { p0, p1 ->
                if (p1) {
                    ibDelete.visibility = View.VISIBLE
                } else {
                    ibDelete.visibility = View.INVISIBLE
                }
            }
        }
    }

    companion object {
        fun createItem(parent: ViewGroup): MarkedViewHolder {
            return MarkedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.marked_item_recycler_view, parent, false)
            )
        }
    }

    class MarkedViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

class MarkedTaskItemDiffCallback: DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}
