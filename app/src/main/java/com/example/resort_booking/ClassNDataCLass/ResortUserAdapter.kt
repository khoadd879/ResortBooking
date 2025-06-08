package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.ApiClient
import com.example.resort_booking.R
import data.FavoriteRequest
import data.FavoriteResponse
import data.Resort
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResortUserAdapter(
    private val resorts: List<Resort>,
    private val context: Context,
    private val onFavoriteChanged: (() -> Unit)? = null // Cho phép callback khi có thay đổi
) : RecyclerView.Adapter<ResortUserAdapter.ResortUserViewHolder>() {

    inner class ResortUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.hotelImageRecommend)
        val name: TextView = itemView.findViewById(R.id.hotelName)
        val location: TextView = itemView.findViewById(R.id.hotelLocation)
        val rating: TextView = itemView.findViewById(R.id.ratingStar)
        val favoriteBtn: ImageButton = itemView.findViewById(R.id.favorite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResortUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resort_user, parent, false)
        return ResortUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResortUserViewHolder, position: Int) {
        val resort = resorts[position]
        holder.name.text = resort.name_rs
        holder.location.text = resort.location_rs
        holder.rating.text = resort.star.toString()

        Glide.with(holder.itemView.context)
            .load(resort.image)
            .placeholder(R.drawable.load_error)
            .into(holder.image)

        holder.favoriteBtn.setImageResource(
            if (resort.favorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        holder.favoriteBtn.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val selectedResort = resorts[currentPosition]

            val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("ID_USER", null)

            if (userId.isNullOrEmpty()) {
                Toast.makeText(context, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiService = ApiClient.create(sharedPref)

            if (selectedResort.favorite) {
                // Gọi API xoá yêu thích
                apiService.deleteFavorite(userId, selectedResort.idRs).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show()
                            selectedResort.favorite = false
                            notifyItemChanged(currentPosition)
                            onFavoriteChanged?.invoke() // Gọi callback nếu có
                        } else {
                            Toast.makeText(context, "Xoá yêu thích thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Gọi API thêm yêu thích
                val body = FavoriteRequest(selectedResort.idRs, userId)
                apiService.createFavorite(body).enqueue(object : Callback<FavoriteResponse> {
                    override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                            selectedResort.favorite = true
                            notifyItemChanged(currentPosition)
                            onFavoriteChanged?.invoke() // Gọi callback nếu có
                        } else {
                            Toast.makeText(context, "Thêm yêu thích thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                        Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int = resorts.size
}
