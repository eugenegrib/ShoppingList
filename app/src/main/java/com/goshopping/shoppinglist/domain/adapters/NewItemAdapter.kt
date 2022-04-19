package com.goshopping.shoppinglist.domain.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.databinding.NewItemBinding
import com.goshopping.shoppinglist.domain.MoreFunctions


/**
 * [ListAdapter] implementation for the recyclerview.
 */

class NewItemAdapter(
    private val moreFunctions: MoreFunctions,
    private val onItemClicked: () -> Unit
) : ListAdapter<String, NewItemAdapter.NewViewHolder>(TaskItemDiffCallback1()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewViewHolder {
        return createItem(parent)
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) {
        val binding = NewItemBinding.bind(holder.view)
        binding.newItem.setOnClickListener{
            onItemClicked()
        }
    }

    companion object {
        fun createItem(parent: ViewGroup): NewViewHolder {
            return NewViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.new_item, parent, false)
            )
        }
    }
    class NewViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

class TaskItemDiffCallback1 : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}