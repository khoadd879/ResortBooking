package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.ClassNDataCLass.RoomAdapter
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ActivityRoomListBinding
import data.Room

class RoomListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomListBinding
    private lateinit var roomAdapter: RoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadRoomData()

        val resortId = intent.getStringExtra("RESORT_ID")

        val btnThemPhong = findViewById<Button>(R.id.btnAddRoom)
        btnThemPhong.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivityForResult(intent, 1001)  // Request code 1001
        }

    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(emptyList())
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRooms.adapter = roomAdapter
    }

    private fun loadRoomData() {
        val rooms = intent.getParcelableArrayListExtra<Room>("ROOM_LIST") ?: emptyList()
        roomAdapter.updateData(rooms)
    }
}