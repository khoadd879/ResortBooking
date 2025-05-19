package com.example.resort_booking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.signIn_layout.LoginActivity
import data.IntrospectRequest
import data.IntrospectResponse
import data.RefreshTokenRequest
import data.LoginResponse
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val accessToken = sharedPref.getString("ACCESS_TOKEN", null)
        val refreshToken = sharedPref.getString("REFRESH_TOKEN", null)

        if (accessToken == null || refreshToken == null) {
            goToLogin()
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://booking-resort-final.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Gọi introspect để kiểm tra access token còn sống không
        val introspectRequest = IntrospectRequest(accessToken)

        apiService.introspect(introspectRequest).enqueue(object : Callback<IntrospectResponse> {
            override fun onResponse(call: Call<IntrospectResponse>, response: Response<IntrospectResponse>) {
                if (response.isSuccessful &&  response.body()?.data?.valid == true) {
                    goToMain()
                } else {
                    refreshAccessToken(apiService, refreshToken)
                }
            }

            override fun onFailure(call: Call<IntrospectResponse>, t: Throwable) {
                goToLogin()
            }
        })

    }

    private fun refreshAccessToken(apiService: ApiService, refreshToken: String) {
        val request = RefreshTokenRequest(refreshToken)

        apiService.refreshToken(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val newToken = response.body()?.data?.token
                    val newRefresh = response.body()?.data?.refreshToken

                    // Lưu token mới vào SharedPreferences
                    val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                    sharedPref.edit().apply {
                        putString("ACCESS_TOKEN", newToken)
                        apply()
                    }

                    val introspectRequest = IntrospectRequest(newToken!!)
                    apiService.introspect(introspectRequest).enqueue(object : Callback<IntrospectResponse> {
                        override fun onResponse(
                            call: Call<IntrospectResponse>,
                            response: Response<IntrospectResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.data?.valid == true) {
                                goToMain()
                            } else {
                                goToLogin()
                            }
                        }
                        override fun onFailure(call: Call<IntrospectResponse>, t: Throwable) {
                            goToLogin()
                        }
                    })

                } else {
                    goToLogin()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                goToLogin()
            }
        })
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
