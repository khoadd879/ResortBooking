package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.app.Activity
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
import com.example.resort_booking.ApiClient
import com.example.resort_booking.ClassNDataCLass.ServiceAdapter
import com.example.resort_booking.R
import data.ServiceListResponse
import data.ServiceWithQuantity
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

class ServiceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceAdapter
    private lateinit var apiService: ApiService
    private lateinit var btnConfirm: Button

    // This list will hold the final, updated selections with full data
    private var selectedServices = mutableListOf<ServiceWithQuantity>()

    private var role: String? = null
    private var resortId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerService)
        btnConfirm = findViewById(R.id.btnConfirmService)
        val btnAddService = findViewById<Button>(R.id.btnAddService)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get data from SharedPreferences and Intent
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        role = sharedPref.getString("ROLE", null)
        resortId = intent.getStringExtra("RESORT_ID")

        // CORE FIX: Get the initial selections passed from BookingDetailActivity
        val initialSelections = intent.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES") ?: arrayListOf()

        // Setup API service
        apiService = ApiClient.create(sharedPref)

        // Setup button visibility based on user role
        btnAddService.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        // Setup Click Listeners
        btnAddService.setOnClickListener {
            val intent = Intent(this, CreateServiceActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        btnConfirm.setOnClickListener {
            Log.d("ServiceActivity", "Sending back updated services: $selectedServices")
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // Load the full service list and merge it with initial selections
        if (resortId != null) {
            loadAndMergeServiceList(resortId!!, initialSelections)
        } else {
            Toast.makeText(this, "Lỗi: Không có ID của resort", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadAndMergeServiceList(resortId: String, initialSelections: List<ServiceWithQuantity>) {
        apiService.getListService(resortId).enqueue(object : Callback<ServiceListResponse> {
            override fun onResponse(call: Call<ServiceListResponse>, response: Response<ServiceListResponse>) {
                if (response.isSuccessful) {
                    val fullApiList = response.body()?.data ?: emptyList()

                    // CORE FIX: Merge the full API list with the initial selections.
                    // The full list from the API is the source of truth for name, price, description.
                    // The initial selections provide the starting quantity.
                    val displayList = fullApiList.map { serviceFromApi ->
                        val matchedSelection = initialSelections.find { it.id_sv == serviceFromApi.idService }
                        ServiceWithQuantity(
                            id_sv = serviceFromApi.idService ?: "",
                            name = serviceFromApi.name_sv ?: "Không rõ",
                            price = serviceFromApi.price ?: BigDecimal.ZERO,
                            describe_service = serviceFromApi.describe_service ?: "",
                            quantity = matchedSelection?.quantity ?: 0 // Use initial quantity if matched
                        )
                    }

                    // Populate the activity's selectedServices list with items that are initially selected
                    selectedServices = displayList.filter { it.quantity > 0 }.toMutableList()

                    adapter = ServiceAdapter(displayList, { service -> handleServiceSelection(service) }, this@ServiceActivity, role)
                    recyclerView.adapter = adapter
                } else {
                    Log.e("ServiceActivity", "Lỗi response: ${response.code()}")
                    Toast.makeText(this@ServiceActivity, "Không tải được danh sách dịch vụ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServiceListResponse>, t: Throwable) {
                Log.e("ServiceActivity", "Lỗi kết nối: ${t.message}", t)
                Toast.makeText(this@ServiceActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // This function's logic was already correct and needs no changes.
    // It correctly modifies the `selectedServices` list based on user interaction.
    private fun handleServiceSelection(service: ServiceWithQuantity) {
        val index = selectedServices.indexOfFirst { it.id_sv == service.id_sv }

        if (service.quantity > 0) {
            // If service exists, update it. If not, add it.
            if (index != -1) {
                selectedServices[index] = service
            } else {
                selectedServices.add(service)
            }
        } else {
            // If quantity is 0 and it exists in the list, remove it.
            if (index != -1) {
                selectedServices.removeAt(index)
            }
        }

        Log.d("ServiceActivity", "Selection updated: ${service.name}, New Quantity: ${service.quantity}")
        Log.d("ServiceActivity", "Current selections count: ${selectedServices.size}")
    }
}