package com.example.testdoan

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val roomId = intent.getIntExtra("roomId", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Room không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val edtName = findViewById<EditText>(R.id.edtItemName)
        val edtQty = findViewById<EditText>(R.id.edtQuantity)
        val spType = findViewById<Spinner>(R.id.spType)
        val btnSave = findViewById<Button>(R.id.btnSaveItem)

        val db = AppDatabase.get(this)

        // 🔹 LOẠI ĐỒ
        val types = listOf("Điện", "Nội thất", "Gia dụng", "Khác")
        spType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        btnSave.setOnClickListener {

            val name = edtName.text.toString().trim()
            val qty = edtQty.text.toString().toIntOrNull() ?: 0
            val type = spType.selectedItem.toString()

            if (name.isEmpty() || qty <= 0) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {

                // 👑 ADMIN THÊM → MẶC ĐỊNH CÒN DÙNG
                db.itemDao().insert(
                    ItemEntity(
                        name = name,
                        quantity = qty,
                        type = type,
                        isBroken = false,   // 👈 mặc định chưa hỏng
                        roomId = roomId
                    )
                )


                withContext(Dispatchers.Main) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }
}
