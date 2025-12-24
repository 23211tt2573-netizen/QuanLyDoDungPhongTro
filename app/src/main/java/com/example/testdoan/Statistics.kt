package com.example.testdoan

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Statistics : AppCompatActivity() {

    private lateinit var tvDien: TextView
    private lateinit var tvNoiThat: TextView
    private lateinit var tvGiaDung: TextView
    private lateinit var tvKhac: TextView

    private var roomId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        tvDien = findViewById(R.id.tvDien)
        tvNoiThat = findViewById(R.id.tvNoiThat)
        tvGiaDung = findViewById(R.id.tvGiaDung)
        tvKhac = findViewById(R.id.tvKhac)

        // 🔹 LẤY roomId
        roomId = intent.getIntExtra("roomId", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Room không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch(Dispatchers.IO) {

            val items: List<ItemEntity> = AppDatabase.get(this@Statistics)
                .itemDao()
                .getItemsByRoom(roomId)

            var dien = 0
            var noiThat = 0
            var giaDung = 0
            var khac = 0

            for (item in items) {
                when (item.type) {
                    "Điện" -> dien += item.quantity
                    "Nội thất" -> noiThat += item.quantity
                    "Gia dụng" -> giaDung += item.quantity
                    else -> khac += item.quantity
                }
            }

            withContext(Dispatchers.Main) {
                tvDien.text = "Điện: $dien"
                tvNoiThat.text = "Nội thất: $noiThat"
                tvGiaDung.text = "Gia dụng: $giaDung"
                tvKhac.text = "Khác: $khac"
            }
        }
    }
}
