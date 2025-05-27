package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.resort_booking.R
import data.CreateExpenseRequest
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import data.CreateExpenseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateExpense : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPreferences)
        val resortId = intent.getStringExtra("RESORT_ID")

        val categoryEditText = findViewById<EditText>(R.id.edtCategory)
        val amountEditText = findViewById<EditText>(R.id.edtAmount)
        val saveButton = findViewById<Button>(R.id.btnSaveExpense)

        saveButton.setOnClickListener {
            val category = categoryEditText.text.toString()
            val amount = amountEditText.text.toString().toBigDecimal()
            val createExpense = CreateExpenseRequest(
                idResort = resortId.toString(),
                category = category,
                amount = amount
            )
            apiService.createExpense(createExpense).enqueue(object : Callback<CreateExpenseResponse> {
                override fun onResponse(call: Call<CreateExpenseResponse?>, response: Response<CreateExpenseResponse?>) {
                    if(response.isSuccessful){
                        Toast.makeText(this@CreateExpense, "Thêm chi tiêu thành công", Toast.LENGTH_SHORT).show()
                    }else{
                        Log.e("CreateExpense", "Error: ${response.code()}")
                        Toast.makeText(this@CreateExpense, "Thêm chi tiêu thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreateExpenseResponse?>, t: Throwable) {
                    Toast.makeText(this@CreateExpense, "Thêm chi tiêu thất bại", Toast.LENGTH_SHORT).show()
                    Log.e("CreateExpense", "Error: ${t.message}")
                }
            })
        }
    }
}