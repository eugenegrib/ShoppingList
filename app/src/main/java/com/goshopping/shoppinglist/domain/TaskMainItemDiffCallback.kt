package com.goshopping.shoppinglist.domain

import androidx.recyclerview.widget.DiffUtil
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem

// DiffUtil.Callback - сравнивает списки
// DiffUtil.ItemCallback - сравнивает отдельные елементы
class TaskMainItemDiffCallback: DiffUtil.ItemCallback<MainItem>() {
    override fun areItemsTheSame(oldItem: MainItem, newItem: MainItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MainItem, newItem: MainItem): Boolean {
        return oldItem == newItem
    }
}