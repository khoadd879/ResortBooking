package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
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
    private var role: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy dữ liệu từ SharedPreferences và Intent
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        resortId = intent.getStringExtra("RESORT_ID")
        role = sharedPref.getString("ROLE", "")
        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val btnDichVu = findViewById<Button>(R.id.ServiceList)
        // Kiểm tra quyền hiển thị nút thêm phòng
        if (role?.contains("ROLE_USER") == true) {
            binding.btnAddRoom.visibility = Button.GONE
            btnDichVu.visibility = Button.GONE
        } else {
            binding.btnAddRoom.visibility = Button.VISIBLE
            btnDichVu.visibility = Button.VISIBLE
        }

        roomAdapter = RoomAdapter(emptyList(), this@RoomListActivity)
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRooms.adapter = roomAdapter

        // Sự kiện thêm phòng
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

        // Gọi API lần đầu
        loadRoomData()

    }

    override fun onResume() {
        super.onResume()
        loadRoomData()
    }

    private fun loadRoomData() {
        val id = resortId ?: return

        apiService.getListRoomById(id).enqueue(object : Callback<RoomResponse> {
            override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val rooms = response.body()?.data ?: emptyList()
                    roomAdapter.updateData(rooms)
                } else {
                    Toast.makeText(this@RoomListActivity, "Không tải được danh sách phòng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                Toast.makeText(this@RoomListActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
