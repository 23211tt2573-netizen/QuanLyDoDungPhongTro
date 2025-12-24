package com.example.testdoan

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val edtUser = findViewById<EditText>(R.id.edtUser)
        val edtPass = findViewById<EditText>(R.id.edtPass)
        val edtRePass = findViewById<EditText>(R.id.edtRePass)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val db = AppDatabase.get(this)

        btnRegister.setOnClickListener {

            val user = edtUser.text.toString().trim()
            val pass = edtPass.text.toString().trim()
            val repass = edtRePass.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != repass) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {

                val id = db.userDao().insert(
                    UserEntity(
                        username = user,
                        password = pass,
                        isAdmin = false
                    )
                )

                withContext(Dispatchers.Main) {

                    getSharedPreferences("USER", MODE_PRIVATE)
                        .edit()
                        .putInt("userId", id.toInt())
                        .putBoolean("isAdmin", false)
                        .apply()

                    Toast.makeText(
                        this@Register,
                        "Đăng ký thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@Register, Login::class.java))
                    finish()
                }
            }
        }
    }
}
