package com.example.resort_booking.signIn_layout

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.resort_booking.R
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.text.TextPaint
import android.util.Log
import data.VerifyOTPResponse

class verificationCodeActivity : AppCompatActivity() {

    private lateinit var textCountDown: TextView
    private lateinit var resendOTPEditText: TextView
    private lateinit var userEmail: String
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification_code)

        textCountDown = findViewById(R.id.textCountDown)
        resendOTPEditText = findViewById(R.id.resendOTP)
        val btnVerify = findViewById<TextView>(R.id.button_verify)
        val otpEditText = findViewById<EditText>(R.id.OTP_Num)

        // Nhận email từ Intent
        userEmail = intent.getStringExtra("email") ?: return

        val fullText = "Không nhận được mã? Gửi lại"
        val spannable = SpannableString(fullText)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                sendOTP(userEmail)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@verificationCodeActivity, R.color.navy)
            }
        }, fullText.indexOf("Gửi lại"), fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        resendOTPEditText.text = spannable
        resendOTPEditText.movementMethod = LinkMovementMethod.getInstance()
        resendOTPEditText.highlightColor = Color.TRANSPARENT

        // Sự kiện nút xác minh OTP
        btnVerify.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOTP(otp, userEmail)
            }
        }

        startOTPTimer()
    }

    private fun startOTPTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                textCountDown.text = "$seconds s"
            }

            override fun onFinish() {
                textCountDown.text = "0 s"
                sendOTP(userEmail)
                Toast.makeText(this@verificationCodeActivity, "Đã tự động gửi lại OTP", Toast.LENGTH_SHORT).show()
                startOTPTimer() // This creates a new timer - you might want to remove this
            }
        }.start()
    }

    private fun sendOTP(email: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://booking-resort-final.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.verifyEmail(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@verificationCodeActivity, "OTP đã được gửi lại", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@verificationCodeActivity, "Không gửi được OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@verificationCodeActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyOTP(otp: String, email: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://booking-resort-final.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.verifyOTP(otp, email).enqueue(object : Callback<VerifyOTPResponse>  {
            override fun onResponse(call: Call<VerifyOTPResponse>, response: Response<VerifyOTPResponse>) {
                if (response.isSuccessful) {
                    // Cancel the timer before proceeding
                    if (::countDownTimer.isInitialized) {
                        countDownTimer.cancel()
                    }

                    val intent = Intent(this@verificationCodeActivity, ChangePasswordActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@verificationCodeActivity, "OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyOTPResponse>, t: Throwable) {
                Toast.makeText(this@verificationCodeActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
