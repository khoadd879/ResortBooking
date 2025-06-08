package com.example.resort_booking.signIn_layout

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.adapter.HistoryTransactionAdapter
import com.example.resort_booking.ApiClient
import com.example.resort_booking.databinding.ActivityHistoryTransactionBinding
import data.Payment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryTransactionBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var paymentAdapter: HistoryTransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val idUser = intent.getIntExtra("idUser", -1)
        if (idUser == -1) {
            Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.rvPaymentHistory.layoutManager = LinearLayoutManager(this)
        fetchPayments(idUser)
    }

    private fun fetchPayments(idUser: Int) {
        val apiService = ApiClient.create(sharedPreferences)

        apiService.getPayments(idUser).enqueue(object : Callback<List<Payment>> {
            override fun onResponse(call: Call<List<Payment>>, response: Response<List<Payment>>) {
                if (response.isSuccessful && response.body() != null) {
                    val payments = response.body()!!
                    paymentAdapter = HistoryTransactionAdapter(payments)
                    binding.rvPaymentHistory.adapter = paymentAdapter
                } else {
                    Toast.makeText(this@HistoryTransactionActivity, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Payment>>, t: Throwable) {
                Log.e("PaymentHistory", "Lỗi API: ${t.message}")
                Toast.makeText(this@HistoryTransactionActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })
    }
}