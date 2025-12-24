package com.example.testdoan

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

class MainActivity : AppCompatActivity() {

    private lateinit var lv: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val rooms = mutableListOf<RoomEntity>()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        isAdmin = getSharedPreferences("USER", MODE_PRIVATE)
            .getBoolean("isAdmin", false)

        lv = findViewById(R.id.lvRoom)

        val btnAddRoom = findViewById<Button>(R.id.btnAddRoom)

        if (!isAdmin) {
            btnAddRoom.visibility = Button.GONE
        }

        btnAddRoom.setOnClickListener {
            startActivity(Intent(this, AddRoom::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        lv.setOnItemClickListener { _, _, position, _ ->
            val room = rooms[position]

            if (isAdmin) {
                // ADMIN: VÀO + XÓA
                val options = arrayOf("Vào phòng", "Xóa phòng")

                AlertDialog.Builder(this)
                    .setTitle(room.name)
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> openRoom(room.id)
                            1 -> deleteRoom(room.id)
                        }
                    }.show()
            } else {
                // USER: CHỈ VÀO PHÒNG
                openRoom(room.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRooms()
    }

    // MỞ PHÒNG
    private fun openRoom(roomId: Int) {
        val i = Intent(this, ItemList::class.java)
        i.putExtra("roomId", roomId)
        startActivity(i)
    }

    // XÓA PHÒNG (ADMIN)
    private fun deleteRoom(roomId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.get(this@MainActivity)
                .roomDao()
                .delete(roomId)
            loadRooms()
        }
    }

    private fun loadRooms() {
        lifecycleScope.launch(Dispatchers.IO) {

            val userId = getSharedPreferences("USER", MODE_PRIVATE)
                .getInt("userId", -1)

            val data = AppDatabase.get(this@MainActivity)
                .roomDao()
                .getAllRooms()

            rooms.clear()
            rooms.addAll(data)

            val names = rooms.map { it.name }

            runOnUiThread {
                adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_list_item_1,
                    names
                )
                lv.adapter = adapter
            }
        }
    }
}
