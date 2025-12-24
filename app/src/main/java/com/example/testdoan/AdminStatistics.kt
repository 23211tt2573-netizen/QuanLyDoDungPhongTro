package com.example.testdoan

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminStatistics : AppCompatActivity() {

    private lateinit var tvStat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_statistics)

        // 🔐 CHỈ ADMIN
        val isAdmin = getSharedPreferences("USER", MODE_PRIVATE)
            .getBoolean("isAdmin", false)

        if (!isAdmin) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvStat = findViewById(R.id.tvAdminStat)

        loadStatistics()
    }

    private fun loadStatistics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.get(this@AdminStatistics).itemDao()

            val total = dao.countAllItems()
            val broken = dao.countBrokenItems()

            withContext(Dispatchers.Main) {
                tvStat.text = """
                    📦 Tổng số đồ dùng: $total
                    🔴 Đồ bị hỏng: $broken
                """.trimIndent()
            }
        }
    }
}
