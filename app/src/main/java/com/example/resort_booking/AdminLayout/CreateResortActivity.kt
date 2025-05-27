package com.example.resort_booking.AdminLayout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import com.google.gson.Gson
import data.CreateResortRequest
import data.CreateResortResponse
import data.CreateRoomRequest
import data.CreateRoomResponse
import data.RoomDraft
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import java.io.File
import java.io.FileOutputStream

class CreateResortActivity : AppCompatActivity() {

    private lateinit var imgResort: ImageView
    private lateinit var layoutPhongContainer: LinearLayout
    private var selectedImageUri: Uri? = null
    private val addedRoomNames = mutableListOf<String>()
    private val roomDraftList = mutableListOf<RoomDraft>()

    private var accessToken: String? = null
    private var idOwner: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val ADD_ROOM_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_resort)

        imgResort = findViewById(R.id.imgResort)
        layoutPhongContainer = findViewById(R.id.layoutPhongContainer)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        idOwner = sharedPref.getString("ID_USER", null)

        findViewById<Button>(R.id.btnChonAnh).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }


        findViewById<Button>(R.id.btnThemPhong).setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivityForResult(intent, ADD_ROOM_REQUEST)
        }

        val btnTaoResort = findViewById<Button>(R.id.btnTaoResort)

       btnTaoResort.setOnClickListener {
            val ten = findViewById<EditText>(R.id.etTenResort).text.toString()
            val diaDiem = findViewById<EditText>(R.id.etDiaDiem).text.toString()
            val moTa = findViewById<EditText>(R.id.etMoTa).text.toString()



            if (ten.isBlank() || diaDiem.isBlank() || moTa.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

           btnTaoResort.isEnabled = false

           val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            val request = CreateResortRequest(
                idOwner = idOwner.toString(),
                name_rs = ten.toString(),
                location_rs = diaDiem.toString(),
                describe_rs = moTa.toString()
            )

            val resortJson = Gson().toJson(request)
            val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart = prepareFilePart("file", selectedImageUri!!)


            apiService.createResort(
                resortRequestBody,
                imagePart,
            ).enqueue(object : Callback<CreateResortResponse> {
                override fun onResponse(
                    call: Call<CreateResortResponse>,
                    response: Response<CreateResortResponse>
                ) {
                    btnTaoResort.isEnabled = true
                    if (response.isSuccessful) {
                        val createdResort = response.body()?.data
                        val id_rs = createdResort?.idRs
                        if (id_rs != null) {
                            if (roomDraftList.isEmpty()) {
                                // Nếu không có phòng để tạo thì chuyển thẳng sang ResortListActivity luôn
                                val message = response.body()?.message ?: "Tạo resort thành công"
                                Toast.makeText(this@CreateResortActivity, message, Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@CreateResortActivity, ResortListActivity::class.java))
                                finish()
                            } else {
                                // Nếu có phòng thì gọi hàm tạo phòng tuần tự
                                createRoomsSequentially(id_rs, roomDraftList.toList())
                                val message = response.body()?.message ?: "Tạo resort thành công"
                                Toast.makeText(this@CreateResortActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Toast.makeText(
                            this@CreateResortActivity,
                            "Tạo resort thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CreateResortResponse>, t: Throwable) {
                    Toast.makeText(
                        this@CreateResortActivity,
                        "Lỗi kết nối: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun createRoomsSequentially(idResort: String, rooms: List<RoomDraft>, index: Int = 0) {
        if (index >= rooms.size) {
            Toast.makeText(this, "Tạo tất cả phòng hoàn tất", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@CreateResortActivity, ResortListActivity::class.java))
            finish()
            return
        }

        val draft = rooms[index]

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        val idRsBody = idResort
        val idTypeBody = draft.type_room
        val nameRoomBody = draft.name_room
        val priceBody = draft.price
        val statusBody = draft.status
        val describeBody = draft.describe_room
        val imageRoom = draft.image
        val imagePart = prepareFilePartFile(imageRoom)

        val requestRoom = CreateRoomRequest(
            id_rs = idRsBody,
            id_type = idTypeBody,
            name_room = nameRoomBody,
            price = priceBody,
            status = statusBody,
            describe_room = describeBody
        )

        val resortJson = Gson().toJson(requestRoom)
        val resortRequestBody = resortJson.toRequestBody("application/json".toMediaTypeOrNull())

        if (imageRoom == null) {
            Toast.makeText(this, "Thiếu ảnh cho phòng ${draft.name_room}", Toast.LENGTH_SHORT).show()
            createRoomsSequentially(idResort, rooms, index + 1)
            return
        }

        apiService.createRoom(
            resortRequestBody,
            imagePart,
        ).enqueue(object : Callback<CreateRoomResponse> {
            override fun onResponse(
                call: Call<CreateRoomResponse>,
                response: Response<CreateRoomResponse>
            ) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Tạo phòng ${draft.name_room} thành công"
                    Toast.makeText(this@CreateResortActivity, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CreateResortActivity, "Không tạo được phòng ${draft.name_room}", Toast.LENGTH_SHORT).show()
                }
                createRoomsSequentially(idResort, rooms, index + 1)
            }

            override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                Toast.makeText(this@CreateResortActivity, "Lỗi khi tạo phòng ${draft.name_room}: ${t.message}", Toast.LENGTH_SHORT).show()
                createRoomsSequentially(idResort, rooms, index + 1)
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

    private fun prepareFilePartFile(file: File?, partName: String = "file"): MultipartBody.Part {
        val requestFile = file!!.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedImageUri = data?.data
                    Glide.with(this).load(selectedImageUri).into(imgResort)
                }
                ADD_ROOM_REQUEST -> {
                    val roomName = data?.getStringExtra("roomName")
                    val roomDraft = data?.getSerializableExtra("roomDraft") as? RoomDraft

                    if (!roomName.isNullOrEmpty() && roomDraft != null) {
                        addedRoomNames.add(roomName)
                        roomDraftList.add(roomDraft)

                        val tv = TextView(this).apply {
                            text = "• $roomName"
                            setTextColor(Color.BLACK)
                            textSize = 14f
                        }
                        layoutPhongContainer.addView(tv)
                    }
                }
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "room_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

}