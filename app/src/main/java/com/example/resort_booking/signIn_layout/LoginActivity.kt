package com.example.resort_booking.signIn_layout

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.auth0.android.jwt.JWT
import com.example.resort_booking.ApiClient
import com.example.resort_booking.MainActivity
import com.example.resort_booking.R
import data.LoginRequest
import data.LoginResponse
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // --- Chuyển sang SignUp ---
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val registerText = "Không có tài khoản? Đăng ký"
        val registerSpannable = SpannableString(registerText)
        registerSpannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.navy)
            }
        }, registerText.indexOf("Đăng ký"), registerText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvRegister.text = registerSpannable
        tvRegister.movementMethod = LinkMovementMethod.getInstance()
        tvRegister.highlightColor = Color.TRANSPARENT

        // --- Chuyển sang Forgot Password ---
        val forgotPasswordText = findViewById<TextView>(R.id.forgot_password)
        val forgotText = "Quên mật khẩu?"
        val forgotSpannable = SpannableString(forgotText)
        forgotSpannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.black)
            }
        }, 0, forgotText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forgotPasswordText.text = forgotSpannable
        forgotPasswordText.movementMethod = LinkMovementMethod.getInstance()
        forgotPasswordText.highlightColor = Color.TRANSPARENT

        // --- Hiện/Ẩn mật khẩu ---
        val passwordEditText = findViewById<EditText>(R.id.password)
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

        // --- Đăng nhập khi nhấn nút ---
        val usernameEditText = findViewById<EditText>(R.id.username)
        val btnLogin = findViewById<Button>(R.id.button_login)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://booking-resort-final.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        btnLogin.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            progressBar.visibility = View.VISIBLE

            val loginRequest = LoginRequest(username, password)

            apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    btnLogin.isEnabled = true
                    progressBar.visibility = View.INVISIBLE
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        val token = data?.token
                        val refreshToken = data?.refreshToken
                        val idUser = data?.idUser

                        if (token.isNullOrEmpty() || refreshToken.isNullOrEmpty() || idUser.isNullOrEmpty()) {
                            progressBar.visibility = View.INVISIBLE
                            Toast.makeText(this@LoginActivity, "Dữ liệu phản hồi không hợp lệ", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val jwt = JWT(token)
                        val role = jwt.getClaim("scope").asString()


                        Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("ACCESS_TOKEN", token)
                            putString("REFRESH_TOKEN", refreshToken)
                            putString("ID_USER", idUser)
                            putString("ROLE", role)
                            apply()
                        }
                        ApiClient.create(sharedPref)
                        ApiClient.authInterceptor?.scheduleAutoRefresh(refreshToken)
                        // Chuyển sang màn hình chính
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@LoginActivity, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}