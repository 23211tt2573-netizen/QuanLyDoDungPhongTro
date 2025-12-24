package com.example.testdoan.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.testdoan.data.entity.UserEntity

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM user WHERE username = :u AND password = :p LIMIT 1")
    suspend fun login(u: String, p: String): UserEntity?
}


