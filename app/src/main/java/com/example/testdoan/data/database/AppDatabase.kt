package com.example.testdoan.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testdoan.data.dao.ItemDao
import com.example.testdoan.data.dao.RoomDao
import com.example.testdoan.data.dao.UserDao
import com.example.testdoan.data.entity.ItemEntity
import com.example.testdoan.data.entity.RoomEntity
import com.example.testdoan.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, RoomEntity::class, ItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doan_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    // ================= CALLBACK SEED DATA =================
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            CoroutineScope(Dispatchers.IO).launch {
                val database = get(context)
                seedDefaultData(database)
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {

            val roomA = RoomEntity(name = "Phòng A")
            val roomB = RoomEntity(name = "Phòng B")
            val roomC = RoomEntity(name = "Phòng C")

            db.roomDao().insert(roomA)
            db.roomDao().insert(roomB)
            db.roomDao().insert(roomC)

            val rooms = db.roomDao().getAllRooms()

            rooms.forEach { room ->
                db.itemDao().insert(
                    ItemEntity(
                        name = "Bóng đèn",
                        quantity = 1,
                        type = "Điện",
                        isBroken = false,
                        roomId = room.id
                    )
                )

                db.itemDao().insert(
                    ItemEntity(
                        name = "Ghế nhựa",
                        quantity = 2,
                        type = "Nội thất",
                        isBroken = false,
                        roomId = room.id
                    )
                )
            }
        }
    }
}
