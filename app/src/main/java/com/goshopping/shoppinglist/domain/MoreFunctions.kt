package com.goshopping.shoppinglist.domain

import android.view.View
import android.widget.EditText
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.domain.adapters.ItemListAdapter

interface MoreFunctions {
    fun clearFocus(){}
    fun updateItem(item: Item){}
    fun requestFocus(view: View){}
    fun deleteItem(item: Item, isChecked:Boolean){}
    fun onStartDrag(holder: ItemListAdapter.ViewHolder)
    fun updateCheckBox(addOrRemove: Boolean, item: Item){}
    var isAddedNewTask: Boolean
    fun showKeyboard(edittext: EditText){}
    fun closeKeyboard(edittext: EditText){}
}