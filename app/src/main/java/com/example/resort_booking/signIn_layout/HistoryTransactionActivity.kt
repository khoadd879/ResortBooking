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
import data.PaymentResponse
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

        sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

        val idUser = sharedPreferences.getString("ID_USER", null)
        if (idUser == "") {
            Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.rvPaymentHistory.layoutManager = LinearLayoutManager(this)
        fetchPayments(idUser.toString())
    }

    private fun fetchPayments(idUser: String) {
        val apiService = ApiClient.create(sharedPreferences)

        apiService.getPayments(idUser).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val payments = response.body()!!.data
                    paymentAdapter = HistoryTransactionAdapter(payments)
                    binding.rvPaymentHistory.adapter = paymentAdapter
                } else {
                    Toast.makeText(this@HistoryTransactionActivity, "Không có dữ liệu: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                Log.e("PaymentHistory", "Lỗi API: ${t.message}")
                Toast.makeText(this@HistoryTransactionActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })
    }
}