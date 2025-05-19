package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.ResortAdapter
import com.example.resort_booking.R
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class ResortListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var resortAdapter: ResortAdapter

    private var accessToken: String? = null
    private var userId: String? = null
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resort_list)

        recyclerView = findViewById(R.id.rvResortList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)
        userId = sharedPref.getString("ID_USER", null)

        val btnThemResort = findViewById<Button>(R.id.btnThemResort)
        btnThemResort.setOnClickListener {
            startActivity(Intent(this, CreateResortActivity::class.java))
        }

        if (accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chưa đăng nhập hoặc thiếu thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        // Gọi lần đầu
        fetchResortsFromServer()
    }

    override fun onResume() {
        super.onResume()
        // Gọi lại khi quay lại activity
        fetchResortsFromServer()
    }

    private fun fetchResortsFromServer() {
        apiService.getResortList(userId!!)
            .enqueue(object : Callback<ResortResponse> {
                override fun onResponse(
                    call: Call<ResortResponse>,
                    response: Response<ResortResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val resortList = response.body()?.data ?: emptyList()
                        resortAdapter = ResortAdapter(resortList, this@ResortListActivity) {
                            fetchResortsFromServer()
                        }
                        recyclerView.adapter = resortAdapter
                    } else {
                        Toast.makeText(this@ResortListActivity, "Không tải được danh sách resort", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                    Toast.makeText(this@ResortListActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
