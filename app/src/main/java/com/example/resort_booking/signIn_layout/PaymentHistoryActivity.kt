package com.example.resort_booking.signIn_layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.PaymentHistoryAdapter
import com.example.resort_booking.R
import interfaceAPI.ApiService

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter
    private var userId: String? = null
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_explore)
        recyclerView = findViewById(R.id.recyclerViewPaymentHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)


    }
}
