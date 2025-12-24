package com.example.testdoan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        val itemId = intent.getIntExtra("itemId", -1)
        if (itemId == -1) {
            Toast.makeText(this, "Item không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val oldName = intent.getStringExtra("name") ?: ""
        val oldQty = intent.getIntExtra("qty", 0)
        val oldType = intent.getStringExtra("type") ?: "Khác"

        val edtName = findViewById<EditText>(R.id.edtItemName)
        val edtQty = findViewById<EditText>(R.id.edtQuantity)
        val spType = findViewById<Spinner>(R.id.spType)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateItem)

        edtName.setText(oldName)
        edtQty.setText(oldQty.toString())

        val types = listOf("Điện", "Nội thất", "Gia dụng", "Khác")
        spType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        spType.setSelection(types.indexOf(oldType))

        val db = AppDatabase.get(this)

        btnUpdate.setOnClickListener {

            val newName = edtName.text.toString().trim()
            val newQty = edtQty.text.toString().toIntOrNull() ?: 0
            val newType = spType.selectedItem.toString()

            if (newName.isEmpty() || newQty <= 0) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                db.itemDao().updateItemInfo(
                    itemId,
                    newName,
                    newQty,
                    newType
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditItem,
                        "Cập nhật thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

        }
    }
}
