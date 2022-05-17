package com.goshopping.shoppinglist.data.room.items

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemDao {

    @Query("SELECT * from item WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    @Query("SELECT * from item WHERE `check` IS 0 ORDER BY position ASC")
    fun getItemsNotCheck(): Flow<List<Item>>

    @Query("SELECT * from item WHERE `check` IS 1 ORDER BY position ASC")
    fun getItemsCheck(): Flow<List<Item>>

    @Query("SELECT * from item WHERE `check` IS 0 AND parent IS :id ORDER BY position ASC")
    fun getItemsNotCheckFromParent(id: Int): Flow<List<Item>>

    @Query("SELECT * from item WHERE `check` IS 1 AND parent IS :id ORDER BY position ASC")
    fun getItemsCheckFromParent(id: Int): Flow<List<Item>>

    @Query("SELECT * from item WHERE parent IS :parent ORDER BY position ASC")
    fun getAllItems(parent: Int): Flow<List<Item>>

    @Query("SELECT * from item ORDER BY position ASC")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * from item WHERE parent IS :parent ORDER BY position ASC")
    fun getAllSuspend(parent: Int): List<Item>

    @Query("DELETE from item WHERE parent = :id")
    suspend fun deleteItemFromCategory(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)
}