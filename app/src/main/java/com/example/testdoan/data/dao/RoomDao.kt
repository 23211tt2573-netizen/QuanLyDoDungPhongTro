package com.example.testdoan.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.testdoan.data.entity.RoomEntity

@Dao
interface RoomDao {

    @Insert
    suspend fun insert(room: RoomEntity) : Long

    @Query("DELETE FROM rooms WHERE id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM rooms")
    suspend fun getAllRooms(): List<RoomEntity>

    @Query("SELECT COUNT(*) FROM rooms")
    fun countAllRooms(): Int
}
