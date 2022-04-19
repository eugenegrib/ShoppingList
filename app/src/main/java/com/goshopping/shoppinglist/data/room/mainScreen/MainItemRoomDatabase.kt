package com.goshopping.shoppinglist.data.room.mainScreen

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [MainItem::class], version = 1, exportSchema = false)
abstract class MainItemRoomDatabase : RoomDatabase() {

    abstract fun mainItemDao(): MainItemDao

    companion object {
        @Volatile
        private var INSTANCE: MainItemRoomDatabase? = null
        fun getDatabase(context: Context): MainItemRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainItemRoomDatabase::class.java,
                    "121"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}