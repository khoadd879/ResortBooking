package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.resort_booking.R
import data.UpdateServiceRequest
import data.UpdateServiceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

class UpdateServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_service)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serviceId = intent.getStringExtra("service_id")?: ""
        val serviceName = intent.getStringExtra("service_name")?: ""
        val servicePrice = intent.getStringExtra("service_price")?: ""
        val serviceDesc = intent.getStringExtra("service_desc")?: ""

        val tenDichVu = findViewById<EditText>(R.id.edtServiceName)
        val moTaDichVu = findViewById<EditText>(R.id.edtServiceDesc)
        val giaDichVu = findViewById<EditText>(R.id.edtServicePrice)

        tenDichVu.setText(serviceName)
        moTaDichVu.setText(serviceDesc)
        giaDichVu.setText(servicePrice)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        val btnUpdate = findViewById<Button>(R.id.btnSaveService)

        val updateRequest = UpdateServiceRequest(
            tenDichVu.text.toString(),
            giaDichVu.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO,
            moTaDichVu.text.toString()
        )

        btnUpdate.setOnClickListener {
            apiService.updateService(serviceId, updateRequest).enqueue(object: Callback<UpdateServiceResponse> {
                override fun onResponse(
                    call: Call<UpdateServiceResponse?>,
                    response: Response<UpdateServiceResponse?>
                ) {
                    if(response.isSuccessful) {
                        Toast.makeText(this@UpdateServiceActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    }else{
                        Log.d("UpdateServiceActivity", "Cập nhật thất bại: ${response.errorBody()?.string()}")
                        Toast.makeText(this@UpdateServiceActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UpdateServiceResponse?>, t: Throwable) {
                    Toast.makeText(this@UpdateServiceActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}