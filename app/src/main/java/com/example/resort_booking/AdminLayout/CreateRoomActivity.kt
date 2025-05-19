package com.example.resort_booking.AdminLayout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.CreateResortActivity
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateRoomRequest
import data.CreateRoomResponse
import data.RoomDraft
import data.TypeRoomResponse
import interfaceAPI.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var imgRoom: ImageView
    private var selectedImageUri: Uri? = null
    private var accessToken: String? = null
    private lateinit var spinnerTypeRoom: Spinner
    private lateinit var roomTypes: List<data.TypeRoom>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        accessToken = sharedPref.getString("ACCESS_TOKEN", null)

        spinnerTypeRoom = findViewById(R.id.spinnerLoaiPhong)
        imgRoom = findViewById(R.id.imgRoom)

        val resortId = intent.getStringExtra("RESORT_ID")


        val etTenPhong = findViewById<EditText>(R.id.etTenPhong)
        val etGiaPhong = findViewById<EditText>(R.id.etGiaPhong)
        val etMoTaPhong = findViewById<EditText>(R.id.etMoTaPhong)

        // Load danh sách loại phòng
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getListTypeRoom().enqueue(object : Callback<TypeRoomResponse> {
            override fun onResponse(call: Call<TypeRoomResponse>, response: Response<TypeRoomResponse>) {
                if (response.isSuccessful) {
                    roomTypes = response.body()?.data ?: emptyList()
                    val roomTypeNames = roomTypes.map { it.nameType }

                    val adapter = ArrayAdapter(this@CreateRoomActivity, android.R.layout.simple_spinner_item, roomTypeNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTypeRoom.adapter = adapter
                } else {
                    Toast.makeText(this@CreateRoomActivity, "Không thể tải loại phòng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TypeRoomResponse>, t: Throwable) {
                Toast.makeText(this@CreateRoomActivity, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<Button>(R.id.btnChonAnhPhong).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        findViewById<Button>(R.id.btnTaoPhong).setOnClickListener {
            val tenPhong = etTenPhong.text.toString()
            val gia = etGiaPhong.text.toString()
            val moTa = etMoTaPhong.text.toString()

            if (tenPhong.isBlank() || gia.isBlank() || moTa.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPosition = spinnerTypeRoom.selectedItemPosition
            if (selectedPosition < 0 || selectedPosition >= roomTypes.size) {
                Toast.makeText(this, "Vui lòng chọn loại phòng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedTypeId = roomTypes[selectedPosition].id_type
            if(resortId == null) {
                val imageFile = uriToFile(selectedImageUri!!)
                val roomDraft = RoomDraft(
                    name_room = tenPhong,
                    type_room = selectedTypeId,
                    price = gia.toDouble(),
                    status = "Available",
                    describe_room = moTa,
                    image = imageFile
                )

                val resultIntent = Intent()
                resultIntent.putExtra("roomDraft", roomDraft)
                resultIntent.putExtra("roomName", tenPhong)
                setResult(Activity.RESULT_OK, resultIntent)
            }else{
                val requestRoom = CreateRoomRequest(
                    id_rs = resortId,
                    id_type = selectedTypeId,
                    name_room = tenPhong,
                    price = gia.toDouble(),
                    status = "Available",
                    describe_room = moTa
                )

                val resortJson = Gson().toJson(requestRoom)
                val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

                val imagePart = prepareFilePart("file", selectedImageUri!!)

                apiService.createRoom(
                    resortRequestBody,
                    imagePart,
                ).enqueue(object: Callback<CreateRoomResponse>{
                    override fun onResponse(
                        call: Call<CreateRoomResponse>,
                        response: Response<CreateRoomResponse>
                    ){
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@CreateRoomActivity,
                                "Tạo phòng thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else {
                            Toast.makeText(this@CreateRoomActivity, "Không tạo được phòng", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                        Toast.makeText(this@CreateRoomActivity, "Lỗi khi tạo phòng", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            finish()
        }
    }

    // Chuyển Uri thành File để gửi sang CreateResortActivity
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "room_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            selectedImageUri = data?.data
            Glide.with(this).load(selectedImageUri).into(imgRoom)
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
}
