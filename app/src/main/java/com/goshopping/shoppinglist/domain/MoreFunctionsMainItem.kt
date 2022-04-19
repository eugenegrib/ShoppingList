package com.goshopping.shoppinglist.domain

import android.view.View
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem

interface MoreFunctionsMainItem {
    fun updateMainItem(mainItem: MainItem){}
    fun deleteMainItem(mainItem: MainItem){}
    fun clearFocus(){}
    fun requestFocus(view: View){}
}