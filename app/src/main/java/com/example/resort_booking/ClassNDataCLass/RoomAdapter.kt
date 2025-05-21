package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.RoomListActivity
import com.example.resort_booking.AdminLayout.UpdateRoomActivity
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.databinding.ItemRoomBinding
import data.Room
import interfaceAPI.ApiService
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomAdapter(private var rooms: List<Room>, private val context: Context) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {



    inner class RoomViewHolder(private val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {

            val context = binding.root.context
            binding.tvRoomName.text = "${room.name_room}"
            binding.tvTyperoom.text = "Loại phòng: ${room.type_room}"
            binding.tvPrice.text = "Giá thuê: ${room.price} VND"
            binding.tvStatus.text = "Tình trạng: ${room.status}"

            Glide.with(binding.ivAvatar.context)
                .load(room.image)
                .into(binding.ivAvatar)

            binding.btnEdit.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, UpdateRoomActivity::class.java)
                intent.putExtra("room_id", room.idRoom)
                intent.putExtra("room_name", room.name_room)
                intent.putExtra("room_type", room.type_room)
                intent.putExtra("room_price", room.price)
                intent.putExtra("room_describe", room.describe_room)
                intent.putExtra("room_status", room.status)
                intent.putExtra("room_image", room.image)
                context.startActivity(intent)
            }

            binding.btnDelete.setOnClickListener {
                val sharedPref = context.getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

                apiService.deleteRoom(room.idRoom).enqueue(object: Callback<Void>{
                    override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Xóa resort thành công", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Xóa resort thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                        Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            val sharedPref = context.getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val role = sharedPref.getString("ROLE", "")

            if(role?.contains("ROLE_USER") == true){
                binding.btnEdit.visibility = Button.GONE
                binding.btnDelete.visibility = Button.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
        holder.itemView.setOnClickListener {
            holder.itemView.setOnClickListener {
                val intent = Intent(context, HotelDetailActivity::class.java)
                intent.putExtra("RESORT_ID", rooms[position].idRoom)
                intent.putExtra("RESORT_NAME", rooms[position].name_room)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = rooms.size

    fun updateData(newRooms: List<Room>) {
        rooms = newRooms
        notifyDataSetChanged()
    }
}
