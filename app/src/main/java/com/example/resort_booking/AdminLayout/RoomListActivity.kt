package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.ClassNDataCLass.RoomAdapter
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ActivityRoomListBinding
import com.example.resort_booking.signIn_layout.ServiceActivity
import data.RoomResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomListBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var apiService: ApiService
    private var resortId: String? = null
    private var isSelectMode: Boolean = false

    // Khai báo btnDichVu ở đây để có thể truy cập trong cả class
    private lateinit var btnDichVu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        resortId = intent.getStringExtra("RESORT_ID")
        isSelectMode = intent.getBooleanExtra("SELECT_MODE", false)

        // Khởi tạo btnDichVu ở đây
        btnDichVu = findViewById(R.id.ServiceList)
        val role = sharedPref.getString("ROLE", "")

        // Hiển thị nút theo role
        if (role?.contains("ROLE_USER") == true || isSelectMode) {
            binding.btnAddRoom.visibility = View.GONE
            btnDichVu.visibility = View.GONE
        } else {
            binding.btnAddRoom.visibility = View.VISIBLE
            btnDichVu.visibility = View.VISIBLE
        }

        // Adapter với click listener nếu là chế độ chọn phòng
        roomAdapter = if (isSelectMode) {
            RoomAdapter(emptyList(), this@RoomListActivity) { room ->
                val resultIntent = Intent()
                resultIntent.putExtra("ROOM_ID", room.idRoom)
                resultIntent.putExtra("ROOM_NAME", room.name_room)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        } else {
            RoomAdapter(emptyList(), this@RoomListActivity, null) // Không xử lý click
        }

        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRooms.adapter = roomAdapter

        binding.btnAddRoom.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        btnDichVu.setOnClickListener {
            val intent = Intent(this, ServiceActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        // Không cần gọi loadRoomData() ở đây vì đã có trong onResume()
    }

    override fun onResume() {
        super.onResume()
        loadRoomData()
    }

    private fun loadRoomData() {
        // --- HIỂN THỊ PROGRESSBAR VÀ VÔ HIỆU HÓA NÚT ---
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddRoom.isEnabled = false
        btnDichVu.isEnabled = false

        resortId?.let { id ->
            apiService.getListRoomById(id).enqueue(object : Callback<RoomResponse> {
                override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                    // --- ẨN PROGRESSBAR VÀ KÍCH HOẠT LẠI NÚT ---
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddRoom.isEnabled = true
                    btnDichVu.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        roomAdapter.updateData(response.body()!!.data ?: emptyList())
                    } else {
                        Log.e("RoomListActivity", "Lỗi tải danh sách phòng: ${response.message()}")
                        Toast.makeText(this@RoomListActivity, "Không tải được danh sách phòng", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                    // --- ẨN PROGRESSBAR VÀ KÍCH HOẠT LẠI NÚT ---
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddRoom.isEnabled = true
                    btnDichVu.isEnabled = true

                    Toast.makeText(this@RoomListActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}