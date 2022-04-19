package com.goshopping.shoppinglist.data.itemForRV


data class ItemForRV(
    val id: Int = 0,
    val itemName: String,
    val itemCheck: Boolean,
    val position:Int,
    val parent:Int
)