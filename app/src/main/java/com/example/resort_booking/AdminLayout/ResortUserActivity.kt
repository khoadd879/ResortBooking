package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.ResortAdapter
import com.example.resort_booking.ClassNDataCLass.ResortUserAdapter
import com.example.resort_booking.R
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResortUserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResortUserAdapter
    private lateinit var apiService: ApiService
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resort_user)

        recyclerView = findViewById(R.id.resortRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        if (userId != null) {
            fetchResortsForUser(userId!!)
        }
    }

    private fun fetchResortsForUser(userId: String) {
        apiService.getResortList(userId).enqueue(object: Callback<ResortResponse>{
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                if (response.isSuccessful) {
                    val resortResponse = response.body()
                    if (resortResponse != null) {
                        val resortList = response.body()?.data ?: emptyList()
                        adapter = ResortUserAdapter(resortList, this@ResortUserActivity) {
                            fetchResortsForUser(userId)
                        }
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@ResortUserActivity, "No resorts found for the user.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                Toast.makeText(this@ResortUserActivity, "Error fetching resorts: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
