package com.example.testdoan

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetail : AppCompatActivity() {

    private lateinit var tvItemDetail: TextView
    private lateinit var cbBroken: CheckBox

    private var itemId = -1
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        tvItemDetail = findViewById(R.id.tvItemDetail)
        cbBroken = findViewById(R.id.cbBroken)

        // 🔐 phân quyền
        isAdmin = getSharedPreferences("USER", MODE_PRIVATE)
            .getBoolean("isAdmin", false)

        itemId = intent.getIntExtra("itemId", -1)
        if (itemId == -1) {
            Toast.makeText(this, "Item không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔴 ADMIN chỉ xem
        cbBroken.isEnabled = !isAdmin

        loadItemDetail()
    }

    private fun loadItemDetail() {
        lifecycleScope.launch(Dispatchers.IO) {
            val item = AppDatabase.get(this@ItemDetail)
                .itemDao()
                .getItemById(itemId)

            if (item == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ItemDetail, "Item không tồn tại", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {

                tvItemDetail.text = """
                    Tên: ${item.name}
                    Số lượng: ${item.quantity}
                    Loại: ${item.type}
                    Trạng thái: ${if (item.isBroken) "Hỏng" else "Còn tốt"}
                """.trimIndent()

                // ⚠️ QUAN TRỌNG: gỡ listener trước
                cbBroken.setOnCheckedChangeListener(null)
                cbBroken.isChecked = item.isBroken

                // 🔵 USER mới được update
                if (!isAdmin) {
                    cbBroken.setOnCheckedChangeListener { _, isChecked ->
                        updateBrokenStatus(isChecked)
                    }
                }
            }
        }
    }

    private fun updateBrokenStatus(isBroken: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.get(this@ItemDetail)
                .itemDao()
                .updateBrokenStatus(itemId, isBroken)
        }
    }
}
