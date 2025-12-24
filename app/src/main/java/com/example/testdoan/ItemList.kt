package com.example.testdoan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testdoan.data.database.AppDatabase
import com.example.testdoan.data.entity.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemList : AppCompatActivity() {

    private lateinit var lv: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<ItemEntity>()

    private var roomId = -1
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        isAdmin = getSharedPreferences("USER", MODE_PRIVATE)
            .getBoolean("isAdmin", false)

        roomId = intent.getIntExtra("roomId", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Room không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lv = findViewById(R.id.lvItem)
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
        lv.adapter = adapter

        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        val btnStat = findViewById<Button>(R.id.btnStat)
        val edtSearch = findViewById<EditText>(R.id.edtSearch)

        if (!isAdmin) btnAddItem.visibility = Button.GONE

        // ADMIN THÊM ĐỒ
        btnAddItem.setOnClickListener {
            val i = Intent(this, AddItem::class.java)
            i.putExtra("roomId", roomId)
            startActivity(i)
        }

        // THỐNG KÊ
        btnStat.setOnClickListener {
            val i = Intent(this, Statistics::class.java)
            i.putExtra("roomId", roomId)
            startActivity(i)
        }

        // SEARCH
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
        })

        // CLICK ITEM
        lv.setOnItemClickListener { _, _, position, _ ->
            val item = items[position]

            if (isAdmin) {
                val options = arrayOf("Sửa", "Xóa", "Xem chi tiết")
                AlertDialog.Builder(this)
                    .setTitle(item.name)
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> editItem(item)
                            1 -> deleteItem(item.id)
                            2 -> viewDetail(item.id)
                        }
                    }.show()
            } else {
                viewDetail(item.id)
            }
        }
    }

    // QUAY LẠI → LOAD LẠI
    override fun onResume() {
        super.onResume()
        loadItems()
    }

    // ================= LOAD ITEMS =================
    private fun loadItems() {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = AppDatabase.get(this@ItemList)
                .itemDao()
                .getItemsByRoom(roomId)

            items.clear()
            items.addAll(data)

            val display = items.map {
                val status = if (it.isBroken) "Hỏng" else "Còn dùng"
                "${it.name} | SL: ${it.quantity} | ${it.type} | $status"
            }

            withContext(Dispatchers.Main) {
                adapter.clear()
                adapter.addAll(display)
                adapter.notifyDataSetChanged()
            }
        }
    }

    // ================= SEARCH =================
    private fun filterItems(query: String) {
        val filtered =
            if (query.isEmpty()) items
            else items.filter { it.name.contains(query, true) }

        val display = filtered.map {
            val status = if (it.isBroken) "Hỏng" else "Còn dùng"
            "${it.name} | SL: ${it.quantity} | ${it.type} | $status"
        }

        adapter.clear()
        adapter.addAll(display)
        adapter.notifyDataSetChanged()
    }

    // ================= ACTION =================
    private fun editItem(item: ItemEntity) {
        val i = Intent(this, EditItem::class.java)
        i.putExtra("itemId", item.id)
        i.putExtra("name", item.name)
        i.putExtra("qty", item.quantity)
        i.putExtra("type", item.type)
        startActivity(i)
    }


    private fun deleteItem(itemId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.get(this@ItemList)
                .itemDao()
                .delete(itemId)

            loadItems()
        }
    }

    private fun viewDetail(itemId: Int) {
        val i = Intent(this, ItemDetail::class.java)
        i.putExtra("itemId", itemId)
        startActivity(i)
    }
}
