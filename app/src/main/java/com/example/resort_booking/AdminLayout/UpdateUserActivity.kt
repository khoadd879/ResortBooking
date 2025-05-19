package com.example.resort_booking.AdminLayout

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateUserResponse
import data.UpdateUserRequest
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
import java.net.URL
import java.util.*

class UpdateUserActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var accessToken: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)

        val tenNguoiDung = findViewById<EditText>(R.id.edtFullName)
        val soDienThoai = findViewById<EditText>(R.id.edtPhone)
        val email = findViewById<EditText>(R.id.edtEmail)
        val cccd = findViewById<EditText>(R.id.edtIdCard)
        val ngaySinh = findViewById<EditText>(R.id.edtDob)
        val hoChieu = findViewById<EditText>(R.id.edtPassport)
        val matKhau = findViewById<EditText>(R.id.edtPassword)
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        val radioGender = findViewById<RadioGroup>(R.id.radioGender)
        val accountUser = findViewById<EditText>(R.id.edtAccount)
        val radioRole = findViewById<RadioGroup>(R.id.radioRole)
        val btnCapNhapnv = findViewById<Button>(R.id.btnUpdateUser)

        // Nhận dữ liệu từ Intent
        val idUser = intent.getStringExtra("id") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val emailUser = intent.getStringExtra("email") ?: ""
        val idCard = intent.getStringExtra("idCard") ?: ""
        val dob = intent.getStringExtra("dob") ?: ""
        val passport = intent.getStringExtra("passport") ?: ""
        val avatar = intent.getStringExtra("avatar") ?: ""
        val sex = intent.getStringExtra("sex") ?: ""
        val account = intent.getStringExtra("account") ?: ""
        val role = intent.getStringExtra("role_user") ?: ""
        val name = intent.getStringExtra("name") ?: ""

        // Gán dữ liệu vào form
        tenNguoiDung.setText(name)
        soDienThoai.setText(phone)
        email.setText(emailUser)
        cccd.setText(idCard)
        ngaySinh.setText(dob)
        hoChieu.setText(passport)
        accountUser.setText(account)

        Glide.with(this)
            .load(avatar)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgAvatar)

        when (role) {
            "USER" -> radioRole.check(R.id.radioUser)
            "MANAGER" -> radioRole.check(R.id.radioManager)
            "ADMIN" -> radioRole.check(R.id.radioAdmin)
        }

        when (sex) {
            "Nam" -> radioGender.check(R.id.radioMale)
            "Nữ" -> radioGender.check(R.id.radioFemale)
        }

        imgAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        ngaySinh.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                    ngaySinh.setText(selectedDate)
                },
                year, month, day)

            datePickerDialog.show()
        }

        btnCapNhapnv.setOnClickListener {
            val selectedRoleId = radioRole.checkedRadioButtonId
            val selectedRole = findViewById<RadioButton>(selectedRoleId).text.toString()
            val roleString = when (selectedRole) {
                "User" -> "USER"
                "Quản lý" -> "MANAGER"
                "Admin" -> "ADMIN"
                else -> "USER"
            }

            val gender = when (radioGender.checkedRadioButtonId) {
                R.id.radioMale -> "Nam"
                R.id.radioFemale -> "Nữ"
                else -> ""
            }

            updateUserInfo(
                role = roleString,
                account = accountUser.text.toString(),
                sex = gender,
                password = matKhau.text.toString(),
                idUser = idUser,
                tenNguoiDung = tenNguoiDung.text.toString(),
                soDienThoai = soDienThoai.text.toString(),
                email = email.text.toString(),
                cccd = cccd.text.toString(),
                ngaySinh = ngaySinh.text.toString(),
                hoChieu = hoChieu.text.toString(),
                matKhau = matKhau.text.toString(),
                imgAvatar = imgAvatar,
                avatarUrl = avatar // 👉 truyền avatar gốc vào
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            findViewById<ImageView>(R.id.imgAvatar).setImageURI(selectedImageUri)
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

    private fun prepareFilePartFromUrl(url: String): MultipartBody.Part? {
        return try {
            val inputStream = URL(url).openStream()
            val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { it.copyTo(outputStream) }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updateUserInfo(
        role: String,
        account: String,
        sex: String,
        password: String,
        idUser: String,
        tenNguoiDung: String,
        soDienThoai: String,
        email: String,
        cccd: String,
        ngaySinh: String,
        hoChieu: String,
        matKhau: String,
        imgAvatar: ImageView,
        avatarUrl: String? // 👈 avatar truyền vào
    ) {
        val requestUser = UpdateUserRequest(
            nameuser = tenNguoiDung,
            phone = soDienThoai,
            email = email,
            identificationCard = cccd,
            dob = ngaySinh,
            passport = hoChieu,
            passworduser = password,
            sex = sex,
            account = account,
            role_user = listOf(role)
        )

        if (tenNguoiDung.isBlank() || soDienThoai.isBlank() || email.isBlank() || cccd.isBlank() ||
            ngaySinh.isBlank() || hoChieu.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val imagePart: MultipartBody.Part? = if (selectedImageUri != null) {
            prepareFilePart("file", selectedImageUri!!)
        } else {
            prepareFilePartFromUrl(avatarUrl ?: "")
        }

        if (imagePart == null) {
            Toast.makeText(this, "Không thể sử dụng ảnh đại diện", Toast.LENGTH_SHORT).show()
            return
        }

        val resortJson = Gson().toJson(requestUser)
        val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())
        val tokenHeader = "Bearer $accessToken"

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.updateUser(idUser, resortRequestBody, imagePart).enqueue(object :
            retrofit2.Callback<CreateUserResponse> {
            override fun onResponse(
                call: Call<CreateUserResponse?>,
                response: Response<CreateUserResponse?>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateUserActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@UpdateUserActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CreateUserResponse?>, t: Throwable) {
                Log.e("CONNECTION_ERROR", "Lỗi: ${t.localizedMessage}", t)
                Toast.makeText(this@UpdateUserActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
