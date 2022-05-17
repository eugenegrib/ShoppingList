package com.goshopping.shoppinglist.domain

import android.app.Application
import com.goshopping.shoppinglist.data.room.items.ItemRoomDatabase
import com.goshopping.shoppinglist.data.room.mainScreen.MainItemRoomDatabase

class StartApplication : Application(){
    val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
    val mainDatabase: MainItemRoomDatabase by lazy { MainItemRoomDatabase.getDatabase(this) }
}