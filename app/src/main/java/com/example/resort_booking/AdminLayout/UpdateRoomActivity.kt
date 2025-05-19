package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateRoomResponse
import data.TypeRoomResponse
import data.UpdateRoomRequest
import interfaceAPI.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class UpdateRoomActivity : AppCompatActivity() {

    private var accessToken: String? = null
    private lateinit var roomTypes: List<data.TypeRoom>
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_room)

        val idRoom = intent.getStringExtra("room_id") ?: ""
        val roomName = intent.getStringExtra("room_name") ?: ""
        val roomType = intent.getStringExtra("room_type") ?: ""
        val roomPrice = intent.getStringExtra("room_price") ?: ""
        val roomDescription = intent.getStringExtra("room_describe") ?: ""
        val roomStatus = intent.getStringExtra("room_status") ?: ""
        val roomImage = intent.getStringExtra("room_image") ?: ""

        val tenPhong = findViewById<EditText>(R.id.editTenPhong)
        val giaPhong = findViewById<EditText>(R.id.edtGiaThue)
        val moTaPhong = findViewById<EditText>(R.id.edtMoTaPhong)
        val imgPhong = findViewById<ImageView>(R.id.imgRoom)

        val spinnerTinhTrang: Spinner = findViewById(R.id.spinnerTinhTrang)
        val statusOptions = listOf("Trống", "Đã được đặt")

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTinhTrang.adapter = statusAdapter

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)

        val spinner = findViewById<Spinner>(R.id.spinnerLoaiPhong)

        tenPhong.setText(roomName)
        giaPhong.setText(roomPrice)
        moTaPhong.setText(roomDescription)
        spinnerTinhTrang.setSelection(statusOptions.indexOf(roomStatus))

        Glide.with(this)
            .load(roomImage)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgPhong)

        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)


        apiService.getListTypeRoom().enqueue(object : Callback<TypeRoomResponse> {
            override fun onResponse(call: Call<TypeRoomResponse>, response: Response<TypeRoomResponse>) {
                if (response.isSuccessful) {
                    roomTypes = response.body()?.data ?: emptyList()
                    val roomTypeNames = roomTypes.map { it.nameType }

                    val adapter = ArrayAdapter(this@UpdateRoomActivity, android.R.layout.simple_spinner_item, roomTypeNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    val selectedTypeIndex = roomTypeNames.indexOfFirst { it.equals(roomType, ignoreCase = true) }
                    if (selectedTypeIndex != -1) {
                        spinner.setSelection(selectedTypeIndex)
                    }
                } else {
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@UpdateRoomActivity, "Không thể tải loại phòng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TypeRoomResponse>, t: Throwable) {
                Toast.makeText(this@UpdateRoomActivity, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<Button>(R.id.btnChonAnhRoom).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        findViewById<Button>(R.id.btnCapNhatRoom).setOnClickListener {
            updateRoomToServer(idRoom, tenPhong.text.toString(), giaPhong.text.toString(), moTaPhong.text.toString(), spinnerTinhTrang, spinner)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            findViewById<ImageView>(R.id.imgRoom).setImageURI(selectedImageUri)
        }
    }

    private fun updateRoomToServer(roomId: String, roomName: String, roomPrice: String, roomDescription: String, spinnerTinhTrang: Spinner, spinner: Spinner) {
        if (roomName.isBlank() || roomPrice.isBlank() || roomDescription.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        val requestRoom = UpdateRoomRequest(
            name_room = roomName,
            id_type = roomTypes[spinner.selectedItemPosition].id_type,
            price = roomPrice.toDouble(),
            describe_room = roomDescription,
            status = spinnerTinhTrang.selectedItem.toString()
        )

        val resortJson = Gson().toJson(requestRoom)
        val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

        val imagePart = prepareFilePart("file", selectedImageUri!!)
        val tokenHeader = "Bearer $accessToken"

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.updateRoom(roomId, resortRequestBody, imagePart).enqueue(object : Callback<CreateRoomResponse> {
            override fun onResponse(call: Call<CreateRoomResponse>, response: Response<CreateRoomResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateRoomActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UpdateRoomActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                Toast.makeText(this@UpdateRoomActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
}
