package com.example.resort_booking.AdminLayout

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.CreateResortActivity
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateUserRequest
import data.CreateUserResponse
import interfaceAPI.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CreateUserActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var edtPhone: EditText
    private lateinit var edtEmail: EditText
    private lateinit var imgAvatar: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button
    private lateinit var edtIdCard: EditText
    private lateinit var edtDob: EditText
    private lateinit var edtPassport: EditText
    private lateinit var edtPassword: EditText
    private var accessToken: String? = null
    private lateinit var edtAccount: EditText

    private var selectedImageUri: Uri? = null

    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)

        // Ánh xạ view
        edtFullName = findViewById(R.id.edtFullName)
        radioGender = findViewById(R.id.radioGender)
        edtPhone = findViewById(R.id.edtPhone)
        edtEmail = findViewById(R.id.edtEmail)
        edtIdCard = findViewById(R.id.edtIdCard)
        edtDob = findViewById(R.id.edtDob)
        edtAccount = findViewById(R.id.edtAccount)
        edtPassport = findViewById(R.id.edtPassport)
        edtPassword = findViewById(R.id.edtPassword)
        imgAvatar = findViewById(R.id.imgAvatar)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSave)

        edtDob.setOnClickListener {
            // Lấy ngày hiện tại để làm mặc định
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    // Định dạng lại thành yyyy-MM-dd
                    val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                    edtDob.setText(selectedDate)  // Gán vào EditText
                },
                year, month, day)

            datePickerDialog.show()
        }


        // Xử lý chọn ảnh
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        imgAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Xử lý nút lưu
        btnSave.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val gender = when (radioGender.checkedRadioButtonId) {
                R.id.radioMale -> "Nam"
                R.id.radioFemale -> "Nữ"
                else -> ""
            }
            val idCard = edtIdCard.text.toString()
            val dob = edtDob.text.toString()
            val account = edtAccount.text.toString()
            val passport = edtPassport.text.toString()
            val password = edtPassword.text.toString()
            if(fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || gender.isEmpty() || idCard.isEmpty() || dob.isEmpty() || account.isEmpty() || passport.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            btnSave.isEnabled = false

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val imagePart = prepareFilePart("file", selectedImageUri!!)

            val requestUser = CreateUserRequest(
                name_user = fullName,
                sex = gender,
                phone = phone,
                email = email,
                identification_card = idCard,
                dob = dob,
                passport = passport,
                account = account,
                passworduser = password
            )

            val resortJson = Gson().toJson(requestUser)
            val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

            val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            apiService.createUser(
                resortRequestBody,
                imagePart
            ).enqueue(object : retrofit2.Callback<CreateUserResponse>{
                override fun onResponse(
                    call: Call<CreateUserResponse>,
                    response: Response<CreateUserResponse>
                ) {
                    btnSave.isEnabled = true
                   if(response.isSuccessful){
                       Toast.makeText(this@CreateUserActivity, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show()
                       startActivity(Intent(this@CreateUserActivity, ResortListActivity::class.java))
                       finish()
                   }else{
                       Log.e("CreateUserActivity", "Response error: ${response.errorBody()?.string()}")
                       Toast.makeText(this@CreateUserActivity, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show()
                   }
                }
                override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                    Toast.makeText(this@CreateUserActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(fileUri)
        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                Glide.with(this).load(it).into(imgAvatar)
            }
        }
    }
}
