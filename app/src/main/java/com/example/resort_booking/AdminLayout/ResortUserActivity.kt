package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.ResortUserAdapter
import com.example.resort_booking.HotelDetailActivity
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
    private lateinit var progressBar: ProgressBar
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resort_user)

        recyclerView = findViewById(R.id.resortRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)

        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        if (userId != null) {
            fetchResortsForUser(userId!!)
        }
    }

    private fun fetchResortsForUser(userId: String) {
        progressBar.visibility = View.VISIBLE
        recyclerView.alpha = 0.5f  // làm mờ RecyclerView trong khi đang tải

        apiService.getResortList(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1f

                if (response.isSuccessful) {
                    val resortResponse = response.body()
                    if (resortResponse != null) {
                        val resortList = resortResponse.data ?: emptyList()
                        adapter = ResortUserAdapter(
                            resortList,
                            this@ResortUserActivity,
                            onFavoriteChanged = {
                                fetchResortsForUser(userId)
                            },
                            onResortClick = { resort ->
                                val intent = Intent(this@ResortUserActivity, HotelDetailActivity::class.java).apply {
                                    putExtra("RESORT_ID", resort.idRs)
                                }
                                startActivity(intent)
                            }
                        )
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            this@ResortUserActivity,
                            "No resorts found for the user.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ResortUserActivity,
                        "Failed to fetch resorts.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1f

                Toast.makeText(
                    this@ResortUserActivity,
                    "Error fetching resorts: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
