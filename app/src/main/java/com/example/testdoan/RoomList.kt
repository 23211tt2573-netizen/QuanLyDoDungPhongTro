package com.example.testdoan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.RoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomList : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ArrayAdapter<String>
    private val roomList = mutableListOf<RoomEntity>()

    private var userId = -1   // ✅ USER THẬT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        val lvRoom = findViewById<ListView>(R.id.lvRoom)
        val btnAdd = findViewById<Button>(R.id.btnAddRoom)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ✅ LẤY USER ID SAU LOGIN
        userId = getSharedPreferences("USER", MODE_PRIVATE)
            .getInt("userId", -1)

        if (userId == -1) {
            finish()
            return
        }

        db = AppDatabase.get(this)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
        lvRoom.adapter = adapter

        // LOAD PHÒNG
        loadRooms()

        btnLogout.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        btnAdd.setOnClickListener {
            startActivityForResult(Intent(this, AddRoom::class.java), 200)
        }

        lvRoom.setOnItemClickListener { _, _, position, _ ->
            val room = roomList[position]

            AlertDialog.Builder(this)
                .setTitle(room.name)
                .setItems(arrayOf("Vào phòng", "Xóa phòng")) { _, which ->
                    when (which) {
                        0 -> {
                            val i = Intent(this, ItemList::class.java)
                            i.putExtra("roomId", room.id)
                            startActivity(i)
                        }
                        1 -> deleteRoom(room)
                    }
                }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadRooms()
    }

    private fun loadRooms() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rooms = db.roomDao().getAllRooms()

            withContext(Dispatchers.Main) {
                roomList.clear()
                roomList.addAll(rooms)

                adapter.clear()
                adapter.addAll(rooms.map { it.name })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteRoom(room: RoomEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.roomDao().delete(room.id)
            loadRooms()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val name = data?.getStringExtra("ROOM") ?: return

            lifecycleScope.launch(Dispatchers.IO) {
                db.roomDao().insert(
                    RoomEntity(name = name, userId = userId)
                )
                loadRooms()
            }
        }
    }
}
