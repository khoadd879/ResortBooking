package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.AdminLayout.CreateServiceActivity
import com.example.resort_booking.ClassNDataCLass.ServiceAdapter
import com.example.resort_booking.R
import data.ServiceListResponse
import data.ServiceWithQuantity
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServiceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceAdapter
    private lateinit var apiService: ApiService
    private val selectedServices = mutableListOf<ServiceWithQuantity>()
    private lateinit var btnDat: Button
    private var role: String? = null
    private var resortId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        recyclerView = findViewById(R.id.recyclerService)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnDat = findViewById(R.id.btnConfirmService)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        role = sharedPref.getString("ROLE", null)

        resortId = intent.getStringExtra("RESORT_ID")

        // Lấy danh sách dịch vụ đã chọn từ intent, nếu có
        val existingServices = intent.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES")
        existingServices?.let {
            selectedServices.clear()
            selectedServices.addAll(it)
        }

        val btnThemService = findViewById<Button>(R.id.btnAddService)
        btnThemService.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        btnThemService.setOnClickListener {
            Log.d("ServiceActivity", "Dịch vụ đã chọn hiện tại: $selectedServices")
            val intent = Intent(this, CreateServiceActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        btnDat.setOnClickListener {
            // Khi bấm đặt mới gửi selectedServices về
            Log.d("ServiceActivity", "Sending services: $selectedServices")
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }

        resortId?.let { loadServiceList(it) }
    }

    private fun loadServiceList(resortId: String) {
        apiService.getListService(resortId).enqueue(object : Callback<ServiceListResponse> {
            override fun onResponse(call: Call<ServiceListResponse>, response: Response<ServiceListResponse>) {
                if (response.isSuccessful) {
                    val serviceList = response.body()?.data ?: emptyList()

                    // Map danh sách dịch vụ với số lượng nếu có trong selectedServices
                    val serviceDisplayList = serviceList.map { service ->
                        val matched = selectedServices.find { it.id_sv == service.idService }
                        ServiceWithQuantity(
                            id_sv = service.idService,
                            name = service.name_sv,
                            price = service.price,
                            describe_service = service.describe_service,
                            quantity = matched?.quantity ?: 0
                        )
                    }

                    adapter = ServiceAdapter(serviceDisplayList, { serviceWithQuantity ->
                        handleServiceSelection(serviceWithQuantity)
                    }, this@ServiceActivity, role)

                    recyclerView.adapter = adapter
                } else {
                    Log.e("ServiceActivity", "Lỗi khi tải danh sách dịch vụ: ${response.code()}")
                    Toast.makeText(this@ServiceActivity, "Không tải được danh sách dịch vụ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServiceListResponse>, t: Throwable) {
                Toast.makeText(this@ServiceActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleServiceSelection(service: ServiceWithQuantity) {
        val existingIndex = selectedServices.indexOfFirst { it.id_sv == service.id_sv }

        if (service.quantity > 0) {
            if (existingIndex >= 0) {
                selectedServices[existingIndex] = service
            } else {
                selectedServices.add(service)
            }
        } else {
            if (existingIndex >= 0) {
                selectedServices.removeAt(existingIndex)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resortId?.let { loadServiceList(it) }
    }
}
