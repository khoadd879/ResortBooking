package com.example.resort_booking.signIn_layout

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        recyclerView = findViewById(R.id.recyclerService)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnDat = findViewById(R.id.btnConfirmService)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", null)

        val btnThemService = findViewById<ImageButton>(R.id.btnAddService)
        btnThemService.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        btnThemService.setOnClickListener {
            val intent = Intent(this, CreateServiceActivity::class.java)
            intent.putExtra("RESORT_ID", intent.getStringExtra("RESORT_ID"))
            startActivity(intent)
        }

        val manage = findViewById<LinearLayout>(R.id.ManageServiceList)
        manage.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val resortId = intent.getStringExtra("RESORT_ID")

        btnDat.setOnClickListener {
            val intent = Intent(this, BookingRoomActivity::class.java)
            intent.putParcelableArrayListExtra(
                "SELECTED_SERVICES",
                ArrayList(selectedServices)
            )
            startActivity(intent)
        }
    }

    private fun loadServiceList(resortId: String) {
        apiService.getListService().enqueue(object : Callback<ServiceListResponse> {
            override fun onResponse(call: Call<ServiceListResponse>, response: Response<ServiceListResponse>) {
                if (response.isSuccessful) {
                    val serviceList = response.body()?.data ?: emptyList()
                    adapter = ServiceAdapter(serviceList) { serviceWithQuantity ->
                        handleServiceSelection(serviceWithQuantity)
                    }
                    recyclerView.adapter = adapter
                } else {
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
