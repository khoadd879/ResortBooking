package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.RoomListActivity
import com.example.resort_booking.AdminLayout.UpdateResortActivity
import com.example.resort_booking.R
import data.Resort
import interfaceAPI.ApiService
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ResortAdapter(
    private val resorts: List<Resort>,
    private val context: Context,
    private val onResortDeleted: (() -> Unit)? = null,
    private val onResortClick: ((Resort) -> Unit)? = null) :

    RecyclerView.Adapter<ResortAdapter.ResortViewHolder>() {

    class ResortViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgResort: ImageView = view.findViewById(R.id.imgResort)
        val tvResortName: TextView = view.findViewById(R.id.tvResortName)
        val tvTotalRooms: TextView = view.findViewById(R.id.tvTotalRooms)
        val tvStarRating: TextView = view.findViewById(R.id.tvStarRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResortViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resort, parent, false)
        return ResortViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResortViewHolder, position: Int) {
        val resort = resorts[position]
        val totalRooms = resorts.sumOf { it.rooms.size }
        holder.tvResortName.text = "Tên: ${resort.name_rs}"
        holder.tvTotalRooms.text = "Số phòng: ${totalRooms}"
        holder.tvStarRating.text = "Đánh giá: ${resort.star}"
        val btnedit = holder.itemView.findViewById<ImageView>(R.id.btnEdit)
        val btndelete = holder.itemView.findViewById<ImageView>(R.id.btnDelete)



        btnedit.setOnClickListener {
            val intent = Intent(context, UpdateResortActivity::class.java)
            intent.putExtra("RESORT_ID", resort.idRs)
            intent.putExtra("RESORT_NAME", resort.name_rs)
            intent.putExtra("RESORT_IMAGE", resort.image)
            intent.putExtra("RESORT_LOCATION", resort.location_rs)
            intent.putExtra("RESORT_DESCRIPTION", resort.describe_rs)
            // Thêm các thông tin khác nếu cần
            context.startActivity(intent)
        }

        btndelete.setOnClickListener {
            val sharedPref = context.getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            apiService.deleteResort(resort.idRs).enqueue(object: Callback<Void>{
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Xóa resort thành công", Toast.LENGTH_SHORT).show()
                        onResortDeleted?.invoke()
                    } else {
                        Toast.makeText(context, "Xóa resort thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        Glide.with(holder.itemView.context)
            .load(resort.image)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgResort)

        holder.itemView.setOnClickListener {
            if (onResortClick != null) {
                onResortClick.invoke(resort)
            } else {
                val intent = Intent(context, RoomListActivity::class.java)
                intent.putExtra("RESORT_ID", resort.idRs)
                context.startActivity(intent)
            }
        }

    }
    override fun getItemCount(): Int = resorts.size
}
