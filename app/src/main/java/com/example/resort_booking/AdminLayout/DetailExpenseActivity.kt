package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.TransactionAdapter
import com.example.resort_booking.R

class DetailExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_expense_acitivity)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)

        // Gán dữ liệu cho Spinner Month
        val spinner = findViewById<Spinner>(R.id.spinnerMonth)
        val months = (1..12).map { "Tháng $it" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        // Gán dữ liệu cho Spinner Year
        val yearList = (2021..2026).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearList)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPreferences)
        val resortId = intent.getStringExtra("RESORT_ID")
        val btnAdd = findViewById<Button>(R.id.btnAddExpense)

        btnAdd.setOnClickListener {
            val intent = Intent(this, CreateExpense::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }


        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val resortId = result.data?.getStringExtra("RESORT_ID")
                Log.d("DetailExpenseActivity", "Resort ID: $resortId")
                if (resortId != null) {
                    // TODO: sử dụng resortId để hiển thị dữ liệu chi tiêu cho resort đó
                }
            }
        }

    }
}

