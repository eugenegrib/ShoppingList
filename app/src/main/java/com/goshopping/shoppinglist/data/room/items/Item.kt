package com.goshopping.shoppinglist.data.room.items

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Item(
   @PrimaryKey(autoGenerate = true)
   val id: Int = 0,
   @ColumnInfo(name = "name")
   val itemName: String,
   @ColumnInfo(name = "check")
   val itemCheck: Boolean,
   @ColumnInfo(name = "position")
   val position:Int,
   @ColumnInfo(name = "parent")
   val parent:Int
)