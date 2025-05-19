package com.example.resort_booking.signIn_layout

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.R
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SetUpAccountActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var sexEditText: EditText
    private lateinit var birthdayEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var idCardEditText: EditText
    private lateinit var passportEditText: EditText
    private lateinit var buttonSetup: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var email: String
    private lateinit var tokenAll: String
    private lateinit var ID: String


    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_up_account)


    }

}
