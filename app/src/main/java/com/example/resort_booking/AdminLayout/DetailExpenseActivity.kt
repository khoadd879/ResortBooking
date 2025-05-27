package com.example.resort_booking.AdminLayout
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.Earn
import com.example.resort_booking.ClassNDataCLass.Expense
import com.example.resort_booking.ClassNDataCLass.TransactionAdapter
import com.example.resort_booking.R


class DetailExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = RecyclerView(this)
        setContentView(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPreferences)
        val resortId = intent.getStringExtra("RESORT_ID")
        val btnAdd = findViewById<Button>(R.id.btnAddExpense)

        btnAdd.setOnClickListener {
            val intent = android.content.Intent(this, CreateExpense::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }


    }
}