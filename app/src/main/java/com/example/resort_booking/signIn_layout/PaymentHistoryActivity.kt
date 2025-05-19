package com.example.resort_booking.signIn_layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.ClassNDataCLass.PaymentHistoryAdapter
import com.example.resort_booking.databinding.ActivityPaymentHistoryBinding

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var adapter: PaymentHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // Sau này bạn sẽ lấy list từ API và gọi:
        // adapter.updateData(listFromApi)
    }

    private fun setupRecyclerView() {
        adapter = PaymentHistoryAdapter(emptyList())
        binding.recyclerViewPaymentHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPaymentHistory.adapter = adapter
    }
}
