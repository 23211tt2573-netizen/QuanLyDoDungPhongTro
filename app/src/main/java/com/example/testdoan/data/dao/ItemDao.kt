package com.example.testdoan.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.testdoan.data.entity.ItemEntity

@Dao
interface ItemDao {

    @Query("SELECT COUNT(*) FROM items")
    suspend fun countAllItems(): Int

    @Query("SELECT COUNT(*) FROM items WHERE isBroken = 1")
    suspend fun countBrokenItems(): Int

    @Query("SELECT COUNT(*) FROM items WHERE roomId = :roomId")
    suspend fun countItemsByRoom(roomId: Int): Int

    @Query("UPDATE items SET isBroken = :broken WHERE id = :id")
    suspend fun updateBrokenStatus(
        id: Int,
        broken: Boolean
    )

    @Query("""
        UPDATE items 
        SET name = :name,
            quantity = :qty,
            type = :type
        WHERE id = :id
    """)
    suspend fun updateItemInfo(
        id: Int,
        name: String,
        qty: Int,
        type: String
    )

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): ItemEntity?

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM items WHERE roomId = :rid")
    suspend fun getItemsByRoom(rid: Int): List<ItemEntity>


    @Insert
    suspend fun insert(item: ItemEntity)
}
