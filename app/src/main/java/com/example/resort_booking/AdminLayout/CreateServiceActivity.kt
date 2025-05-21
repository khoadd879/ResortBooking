package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.R
import data.CreateServiceRequest
import data.CreateServiceResponse
import interfaceAPI.ApiService
import retrofit2.Callback

class CreateServiceActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_service)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        val tenDichVu = findViewById<EditText>(R.id.edtServiceName)
        val moTaDichVu = findViewById<EditText>(R.id.edtServiceDesc)
        val giaDichVu = findViewById<EditText>(R.id.edtServicePrice)

        val resortId = intent.getStringExtra("RESORT_ID")

        val btnCreateService = findViewById<Button>(R.id.btnSaveService)


        btnCreateService.setOnClickListener {
            val ten = tenDichVu.text.toString()
            val moTa = moTaDichVu.text.toString()
            val gia = giaDichVu.text.toString()
            val giaBigDecimal = gia.toBigDecimal()
            btnCreateService.isEnabled = false
            val serviceRequest = CreateServiceRequest(resortId.toString(), ten, giaBigDecimal, moTa)

            apiService.createService(serviceRequest)
                .enqueue(object : Callback<CreateServiceResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<CreateServiceResponse>,
                        response: retrofit2.Response<CreateServiceResponse>
                    ) {
                        if (response.isSuccessful) {
                            val createServiceResponse = response.body()
                            if (createServiceResponse != null) {
                                // Xử lý thành công
                                Toast.makeText(this@CreateServiceActivity, "Tạo dịch vụ thành công", Toast.LENGTH_SHORT).show()
                            }else{
                                btnCreateService.isEnabled = true
                                Toast.makeText(this@CreateServiceActivity, "Tạo dịch vụ thất bại", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<CreateServiceResponse>, t: Throwable) {
                        btnCreateService.isEnabled = true
                        Toast.makeText(this@CreateServiceActivity, "Tạo dịch vụ thất bại", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}