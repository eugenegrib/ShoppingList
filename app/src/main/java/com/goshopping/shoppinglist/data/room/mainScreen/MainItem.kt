package com.goshopping.shoppinglist.data.room.mainScreen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MainItem(
   @PrimaryKey(autoGenerate = true)
   val id: Int = 0,

   @ColumnInfo(name = "parentname")
   val parentName: String,

   @ColumnInfo(name = "position")
   val position:Int,

   @ColumnInfo(name = "allitems")
   var allItems:Int,

   @ColumnInfo(name = "checkeditms")
   val checkedItems:Int,
)