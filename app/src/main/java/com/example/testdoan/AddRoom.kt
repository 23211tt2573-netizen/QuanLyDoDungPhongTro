package com.example.testdoan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.ItemEntity
import com.example.testdoan.data.entity.RoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddRoom : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_room)

        val edtName = findViewById<EditText>(R.id.edtRoomName)
        val btnSave = findViewById<Button>(R.id.btnSaveRoom)

        val db = AppDatabase.get(this)

        val pref = getSharedPreferences("USER", MODE_PRIVATE)
        val userId = pref.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "Lỗi đăng nhập", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSave.setOnClickListener {

            val name = edtName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Nhập tên phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {

                // 1️⃣ INSERT PHÒNG – LẤY roomId
                val roomId = db.roomDao().insert(
                    RoomEntity(
                        name = name,
                        userId = userId
                    )
                ).toInt()

                // 2️⃣ INSERT ĐỒ MẶC ĐỊNH CHO PHÒNG MỚI
                val defaultItems = listOf(
                    ItemEntity(
                        name = "Giường",
                        quantity = 1,
                        type = "Nội thất",
                        isBroken = false,
                        roomId = roomId
                    ),
                    ItemEntity(
                        name = "Tủ lạnh",
                        quantity = 2,
                        type = "Điện",
                        isBroken = false,
                        roomId = roomId
                    )
                )

                defaultItems.forEach {
                    db.itemDao().insert(it)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddRoom,
                        "Đã thêm phòng",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK) // 🔥 QUAN TRỌNG
                    finish()
                }
            }
        }
    }
}
