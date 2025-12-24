package com.example.testdoan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtUser = findViewById<EditText>(R.id.edtUser)
        val edtPass = findViewById<EditText>(R.id.edtPass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val db = AppDatabase.get(this)

        btnLogin.setOnClickListener {
            val u = edtUser.text.toString().trim()
            val p = edtPass.text.toString().trim()

            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔴 1. ADMIN (CỨNG TRONG CODE)
            if (u == AdminConfig.USERNAME && p == AdminConfig.PASSWORD) {
                getSharedPreferences("USER", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isAdmin", true)
                    .apply()

                // ✅ ADMIN VÀO MAIN
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // 🔵 2. USER (ROOM DATABASE)
            lifecycleScope.launch(Dispatchers.IO) {
                val user = db.userDao().login(u, p)

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        getSharedPreferences("USER", MODE_PRIVATE)
                            .edit()
                            .putInt("userId", user.id)
                            .putBoolean("isAdmin", false)
                            .apply()

                        // ✅ USER VÀO MAIN (KHÔNG VÀO STATISTICS)
                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@Login,
                            "Sai tài khoản hoặc mật khẩu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
