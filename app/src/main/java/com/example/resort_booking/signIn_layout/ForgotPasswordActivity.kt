package com.example.resort_booking.signIn_layout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.R
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class ForgotPasswordActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        val emailEditText = findViewById<EditText>(R.id.emailForgot)
        val btnSendOTP = findViewById<Button>(R.id.btn_sendOTP)



        btnSendOTP.setOnClickListener {
            val email = emailEditText.text.toString().trim()



            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gửi API qua Retrofit hoặc gọi trực tiếp (nếu không có body)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://booking-resort-final.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            // Gọi API: /forgotPassword/verifyEmail/{email}
            apiService.verifyEmail(email).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ForgotPasswordActivity, "OTP đã được gửi tới email", Toast.LENGTH_SHORT).show()
                        // Có thể chuyển sang màn hình nhập OTP tại đây
                        startActivity(Intent(this@ForgotPasswordActivity, verificationCodeActivity::class.java))

                        val intent = Intent(this@ForgotPasswordActivity, verificationCodeActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Không gửi được OTP. Email có tồn tại không?", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ForgotPasswordActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }


}