package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.R
import data.RegisterRequest
import data.RegisterResponse
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.example.resort_booking.AdminLayout.UpdateUserActivity

class SignUpActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val re_passwordEditText = findViewById<EditText>(R.id.re_password)
        val signUpButton = findViewById<Button>(R.id.button_SignUp)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://booking-resort-final.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = re_passwordEditText.text.toString().trim()

            // Kiểm tra rỗng
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu khớp
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpButton.isEnabled = false

            progressBar.visibility = View.VISIBLE

            val request = RegisterRequest(username, email, password, confirmPassword)

            apiService.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    signUpButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == true) {
                            Toast.makeText(this@SignUpActivity, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                            //chuyển sang Set up account
                            startActivity(Intent(this@SignUpActivity, UpdateUserActivity::class.java))
                            finish()

                            val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                            sharedPref.edit().putString("EMAIL", email).apply()

                        } else {
                            Toast.makeText(this@SignUpActivity, result?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, "Đăng ký lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    signUpButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SignUpActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

    }
}
