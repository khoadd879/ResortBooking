package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateResortResponse
import data.UpdateResortRequest
import interfaceAPI.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import retrofit2.Call
import retrofit2.*

import retrofit2.Response

class UpdateResortActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var accessToken : String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_resort)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)

        val resortId = intent.getStringExtra("RESORT_ID") ?: ""
        val resortName = intent.getStringExtra("RESORT_NAME") ?: ""
        val resortImage = intent.getStringExtra("RESORT_IMAGE") ?: ""
        val resortLocation = intent.getStringExtra("RESORT_LOCATION") ?: ""
        val resortDescription = intent.getStringExtra("RESORT_DESCRIPTION") ?: ""

        val tenResort = findViewById<EditText>(R.id.edtTenResort)
        val diaDiem = findViewById<EditText>(R.id.edtDiaDiem)
        val moTa = findViewById<EditText>(R.id.edtMoTa)
        val imgResort = findViewById<ImageView>(R.id.imgResort)

        tenResort.setText(resortName)
        diaDiem.setText(resortLocation)
        moTa.setText(resortDescription)

        // Load image using Glide
        Glide.with(this)
            .load(resortImage)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgResort)

        findViewById<Button>(R.id.btnChonAnh).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        findViewById<Button>(R.id.btnCapNhat).setOnClickListener {
            updateResortToServer(resortId.toString(), tenResort.text.toString(), diaDiem.text.toString(), moTa.text.toString(), imgResort)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            findViewById<ImageView>(R.id.imgResort).setImageURI(selectedImageUri)
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

    private fun updateResortToServer(resortID: String, tenResort: String, diaDiem: String, moTa: String, imgResort: ImageView){
        val requestResort = UpdateResortRequest(
            name_rs = tenResort,
            location_rs = diaDiem,
            describe_rs = moTa
        )

        if (tenResort.isBlank() || diaDiem.isBlank() || moTa.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        val resortJson = Gson().toJson(requestResort)
        val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

        val imagePart = prepareFilePart("file", selectedImageUri!!)
        val tokenHeader = "Bearer $accessToken"

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.updateResort(resortID, resortRequestBody, imagePart, tokenHeader).enqueue(object: Callback<CreateResortResponse>{
            override fun onResponse(
                call: Call<CreateResortResponse>,
                response: Response<CreateResortResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateResortActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error body: $errorBody")
                    Toast.makeText(this@UpdateResortActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onFailure(call: Call<CreateResortResponse>, t: Throwable) {
                Toast.makeText(this@UpdateResortActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}