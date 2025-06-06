package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class ResortListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var resortAdapter: ResortAdapter

    private var userId: String? = null
    private lateinit var apiService: ApiService
    private var isSelectMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resort_list)

        recyclerView = findViewById(R.id.rvResortList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)

        isSelectMode = intent.getBooleanExtra("SELECT_MODE", false)

        val btnThemResort = findViewById<Button>(R.id.btnThemResort)
        btnThemResort.setOnClickListener {
            startActivity(Intent(this, CreateResortActivity::class.java))
        }

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chưa đăng nhập hoặc thiếu thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        fetchResortsFromServer()
    }


    override fun onResume() {
        super.onResume()
        // Gọi lại khi quay lại activity
        fetchResortsFromServer()
    }

    private fun fetchResortsFromServer() {
        apiService.getResortListCreated(userId!!)
            .enqueue(object : Callback<ResortResponse> {
                override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val resortList = response.body()?.data ?: emptyList()
                        resortAdapter = ResortAdapter(
                            resortList,
                            this@ResortListActivity,
                            onResortDeleted = { fetchResortsFromServer() },
                            onResortClick = if (isSelectMode) { selectedResort ->
                                val resultIntent = Intent()
                                resultIntent.putExtra("RESORT_ID", selectedResort.idRs)
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            } else null
                        )
                        recyclerView.adapter = resortAdapter
                    } else {
                        Log.e("ResortListActivity", "Lỗi khi tải danh sách resort: ${response.code()}")
                        Toast.makeText(this@ResortListActivity, "Không tải được danh sách resort", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                    Log.e("ResortListActivity", "Lỗi kết nối: ${t.message}", t)
                    Toast.makeText(this@ResortListActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
