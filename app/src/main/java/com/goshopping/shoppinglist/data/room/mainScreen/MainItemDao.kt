package com.goshopping.shoppinglist.data.room.mainScreen

import androidx.room.*
import com.goshopping.shoppinglist.data.room.items.Item
import kotlinx.coroutines.flow.Flow


@Dao
interface MainItemDao {

    @Query("SELECT * from mainitem WHERE id = :id")
    fun getItem(id: Int): Flow<MainItem>

    @Query("SELECT * from mainitem WHERE id IS :id")
     fun getCategory(id: Int): Flow<MainItem>

    @Query("SELECT * from mainitem ORDER BY position ASC")
    fun getAllItems(): Flow<List<MainItem>>

    @Query("DELETE from mainitem WHERE id is :parent")
    suspend fun deleteFromParent(parent:Int)

    @Query("DELETE from mainitem")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MainItem)

    @Update
    suspend fun update(item: MainItem)

    @Delete
    suspend fun delete(item: MainItem)
}