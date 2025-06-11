package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.AdminLayout.adapter.UserAdapter
import com.example.resort_booking.R
import data.ListUserResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnThemUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recyclerView = findViewById(R.id.recyclerViewUsers)
        progressBar = findViewById(R.id.progressBar)
        btnThemUser = findViewById(R.id.btnThemUser)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnThemUser.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUsersFromServer()
    }

    private fun fetchUsersFromServer() {
        progressBar.visibility = View.VISIBLE
        btnThemUser.isEnabled = false

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getListUser()
            .enqueue(object : Callback<ListUserResponse> {
                override fun onResponse(
                    call: Call<ListUserResponse>,
                    response: Response<ListUserResponse>
                ) {
                    progressBar.visibility = View.GONE
                    btnThemUser.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            this@UserListActivity,
                            "Tải danh sách user thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        val userList = response.body()?.data ?: emptyList()
                        recyclerView.adapter = UserAdapter(userList, this@UserListActivity)
                    } else {
                        Toast.makeText(
                            this@UserListActivity,
                            "Không tải được danh sách user",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("UserListActivity", "Response error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ListUserResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnThemUser.isEnabled = true

                    Toast.makeText(
                        this@UserListActivity,
                        "Lỗi kết nối: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
