package com.example.resort_booking

import android.content.SharedPreferences
import android.util.Log
import data.RefreshTokenRequest
import interfaceAPI.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicBoolean

class AuthInterceptor(
    private val sharedPreferences: SharedPreferences,
    baseUrl: String
) : Interceptor {

    private val refreshApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val isRefreshing = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            Log.d("AuthInterceptor", "Got 401, trying refresh token")

            val refreshToken = sharedPreferences.getString("REFRESH_TOKEN", null) ?: return response

            val newToken = runBlocking {
                refreshAccessToken(refreshToken)
            }

            return if (newToken != null) {
                Log.d("AuthInterceptor", "Retrying with new token")
                request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                    .let { chain.proceed(it) }
            } else {
                Log.e("AuthInterceptor", "Refresh token failed, returning original 401")
                response
            }
        }

        return response
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthInterceptor", "Start refreshing token with refreshToken: $refreshToken")
            val call = refreshApiService.refreshToken(RefreshTokenRequest(refreshToken))
            val res = call.execute()

            if (res.isSuccessful) {
                val token = res.body()?.data?.token
                Log.d("AuthInterceptor", "Got new token: $token")

                if (!token.isNullOrEmpty()) {
                    sharedPreferences.edit().apply {
                        putString("ACCESS_TOKEN", token)
                        apply()
                    }
                    scheduleAutoRefresh(refreshToken.toString())
                    token
                } else {
                    Log.e("AuthInterceptor", "Token or refresh token is null or empty")
                    null
                }
            } else {
                Log.e("AuthInterceptor", "Failed to refresh token: ${res.errorBody()?.string() ?: "unknown error"}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Exception during token refresh", e)
            null
        }
    }

    fun scheduleAutoRefresh(refreshToken: String) {
        if (isRefreshing.get()) {
            Log.d("AuthInterceptor", "Auto refresh already scheduled, ignoring")
            return
        }

        isRefreshing.set(true)

        val delayMs = 270_000L // 4 phút 30 giây
        Log.d("AuthInterceptor", "Scheduling auto refresh in ${delayMs / 1000} seconds")

        CoroutineScope(Dispatchers.IO).launch {
            delay(delayMs)
            Log.d("AuthInterceptor", "Auto refreshing token...")
            refreshAccessToken(refreshToken)
            isRefreshing.set(false)
        }
    }
}


object ApiClient {
    private const val BASE_URL = "https://booking-resort-final.onrender.com/"

    var authInterceptor: AuthInterceptor? = null

    fun create(sharedPreferences: SharedPreferences): ApiService {
        authInterceptor = AuthInterceptor(sharedPreferences, BASE_URL)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor!!)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}

