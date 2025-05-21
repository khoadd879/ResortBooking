package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import data.FavouriteListData
import data.ResortDetailResponse
import retrofit2.Call
import retrofit2.Response

class FavouriteAdapter(
    private val favouriteList: List<FavouriteListData>,
    private val onItemClick: (FavouriteListData) -> Unit,
    private val onFavoriteClick: (FavouriteListData) -> Unit
) : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    inner class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resortImage: ImageView = itemView.findViewById(R.id.hotelImageRecommend)
        val resortName: TextView = itemView.findViewById(R.id.hotelName)
        val resortLocation: TextView = itemView.findViewById(R.id.hotelLocation)
        val ratingText: TextView = itemView.findViewById(R.id.ratingStar)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val favourite = favouriteList[position]
        val sharedPref = holder.itemView.context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)

        apiService.getResortById(favourite.resortId, userId.toString()).enqueue(object : retrofit2.Callback<ResortDetailResponse> {
            override fun onResponse(
                call: Call<ResortDetailResponse>,
                response: Response<ResortDetailResponse>
            ) {
                if (response.isSuccessful) {
                    val resort = response.body()?.data
                    if (resort != null) {
                        holder.resortName.text = resort.name_rs
                        holder.resortLocation.text = resort.location_rs
                        holder.ratingText.text = resort.star.toString()

                        Glide.with(holder.itemView.context)
                            .load(resort.image)
                            .into(holder.resortImage)
                    } else {
                        Toast.makeText(holder.itemView.context, "Không tìm thấy resort", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FavouriteAdapter", "Lỗi khi tải resort: ${response.code()}")
                    Toast.makeText(holder.itemView.context, "Tải resort thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResortDetailResponse>, t: Throwable) {
                Toast.makeText(holder.itemView.context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })


        holder.itemView.setOnClickListener {
            onItemClick(favourite)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(favourite)
            holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    override fun getItemCount(): Int = favouriteList.size
}
