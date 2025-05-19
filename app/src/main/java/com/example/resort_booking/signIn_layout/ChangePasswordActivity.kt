package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.R

import data.ChangePasswordRequest
import data.ChangePasswordResponse

import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var userEmail: String;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)

        val passwordEditText = findViewById<EditText>(R.id.password)
        val re_passwordEditText = findViewById<EditText>(R.id.confirmPassword)

        userEmail = intent.getStringExtra("email") ?: run {
            Toast.makeText(this, "Email hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        var isPasswordVisible = false
        passwordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = passwordEditText.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (passwordEditText.right - drawable.bounds.width() - passwordEditText.paddingEnd)
                ) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.outline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0
                        )
                    } else {
                        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.outline_lock_24, 0, R.drawable.baseline_visibility_24, 0
                        )
                    }
                    passwordEditText.setSelection(passwordEditText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        var isRePasswordVisible = false
        re_passwordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = re_passwordEditText.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (re_passwordEditText.right - drawable.bounds.width() - re_passwordEditText.paddingEnd)
                ) {
                    isRePasswordVisible = !isRePasswordVisible
                    if (isRePasswordVisible) {
                        re_passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        re_passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.outline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0
                        )
                    } else {
                        re_passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        re_passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.outline_lock_24, 0, R.drawable.baseline_visibility_24, 0
                        )
                    }
                    re_passwordEditText.setSelection(re_passwordEditText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        val btn_changePassword = findViewById<Button>(R.id.btn_ChangePassword)

        btn_changePassword.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            val confirmPassword = re_passwordEditText.text.toString()
            val request = ChangePasswordRequest(newPassword, confirmPassword)

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu khớp
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val retrofit = Retrofit.Builder()
                .baseUrl("https://booking-resort-final.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            apiService.changePassword(userEmail, request).enqueue(object : Callback<ChangePasswordResponse> {
                override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                    if (response.isSuccessful) {

                        Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                        // Có thể chuyển sang màn hình nhập OTP tại đây
                        startActivity(Intent(this@ChangePasswordActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {

                    Toast.makeText(this@ChangePasswordActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}