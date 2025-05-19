package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import data.Resort

class HotelRecommendAdapter(
    private val resortList: List<Resort>,
    private val onItemClick: (Resort) -> Unit
) : RecyclerView.Adapter<HotelRecommendAdapter.HotelViewHolder>() {

    inner class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvResortName: TextView = itemView.findViewById(R.id.hotelName)
        private val tvLocation: TextView = itemView.findViewById(R.id.hotelLocation)
        private val ratingStar: TextView = itemView.findViewById(R.id.ratingStar)
        private val resortImage: ImageView = itemView.findViewById(R.id.hotelImageRecommend)

        fun bind(resort: Resort) {
            tvResortName.text = resort.name_rs
            tvLocation.text = resort.location_rs
            ratingStar.text = resort.star.toString()

            Glide.with(itemView.context)
                .load(resort.image)
                .placeholder(R.drawable.google)
                .error(R.drawable.hotel)
                .into(resortImage)

            itemView.setOnClickListener {
                onItemClick(resort)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(resortList[position])
    }

    override fun getItemCount(): Int = resortList.size
}
