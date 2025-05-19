package com.example.resort_booking

import android.content.SharedPreferences
import android.util.Log
import data.RefreshTokenRequest
import data.LoginResponse
import interfaceAPI.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(
    private val sharedPreferences: SharedPreferences,
    baseUrl: String
) : Interceptor {

    // Retrofit riêng để refresh token, không có interceptor tránh vòng lặp
    private val refreshApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(request)

        // Nếu nhận 401 Unauthorized -> thử refresh token
        if (response.code == 401) {
            Log.d("AuthInterceptor", "Got 401, trying refresh token")

            val refreshToken = sharedPreferences.getString("REFRESH_TOKEN", null) ?: return response

            val newToken = runBlocking {
                try {
                    val call = refreshApiService.refreshToken(RefreshTokenRequest(refreshToken))
                    // Cần khai báo rõ kiểu Response<LoginResponse>
                    val res: retrofit2.Response<LoginResponse> = call.execute()

                    if (res.isSuccessful) {
                        val token = res.body()?.data?.token
                        val refresh = res.body()?.data?.refreshToken
                        if (!token.isNullOrEmpty() && !refresh.isNullOrEmpty()) {
                            sharedPreferences.edit().apply {
                                putString("ACCESS_TOKEN", token)
                                putString("REFRESH_TOKEN", refresh)
                                apply()
                            }
                            token
                        } else null
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            return if (newToken != null) {
                val newRequest = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                chain.proceed(newRequest)
            } else {
                response
            }
        }

        return response
    }
}

object ApiClient {
    private const val BASE_URL = "https://booking-resort-final.onrender.com/"

    fun create(sharedPreferences: SharedPreferences): ApiService {
        val authInterceptor = AuthInterceptor(sharedPreferences, BASE_URL)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // AuthInterceptor trước
            .addInterceptor(logging)           // Logging sau cùng
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // GẮN OKHTTP VÀO RETROFIT TẠI ĐÂY
            .build()

        return retrofit.create(ApiService::class.java)
    }
}

