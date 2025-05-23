package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        recyclerView = findViewById(R.id.recyclerService)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnDat = findViewById(R.id.btnConfirmService)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", null)

        val btnThemService = findViewById<Button>(R.id.btnAddService)
        btnThemService.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        btnThemService.setOnClickListener {
            Log.d("ServiceActivity", "Dịch vụ đã chọn: $selectedServices")
            val intent = Intent(this, CreateServiceActivity::class.java)
            intent.putExtra("RESORT_ID", intent.getStringExtra("RESORT_ID"))
            startActivity(intent)
        }

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val resortId = intent.getStringExtra("RESORT_ID")

        btnDat.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        loadServiceList(resortId.toString())
    }

    private fun loadServiceList(resortId: String) {
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", null)

        apiService.getListService(resortId).enqueue(object : Callback<ServiceListResponse> {
            override fun onResponse(call: Call<ServiceListResponse>, response: Response<ServiceListResponse>) {
                if (response.isSuccessful) {
                    val serviceList = response.body()?.data ?: emptyList()
                    adapter = ServiceAdapter(serviceList, { serviceWithQuantity ->
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
        if (existingIndex >= 0) {
            selectedServices[existingIndex] = service
        } else {
            selectedServices.add(service)
        }
    }

    override fun onResume() {
        super.onResume()
        val resortId = intent.getStringExtra("RESORT_ID")
        resortId?.let { loadServiceList(it) }
    }
}
