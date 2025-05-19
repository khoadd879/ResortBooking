package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import data.Resort

class HotelAdapter(
    private val resortList: List<Resort>,
    private val onItemClick: (Resort) -> Unit,
    private val onFavoriteClick: (Resort) -> Unit
) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    inner class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvResortName: TextView = itemView.findViewById(R.id.tvResortName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val resortImage: ImageView = itemView.findViewById(R.id.hotelImage)
        private val ratingStar: TextView = itemView.findViewById(R.id.ratingStar)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)

        fun bind(resort: Resort) {
            tvResortName.text = resort.name_rs
            tvLocation.text = resort.location_rs
            ratingStar.text = resort.star.toString()

            Glide.with(itemView.context)
                .load(resort.image)
                .placeholder(R.drawable.hotel)
                .error(R.drawable.load_error)
                .into(resortImage)

            itemView.setOnClickListener {
                onItemClick(resort)
            }

            favoriteButton.setOnClickListener {
                onFavoriteClick(resort)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_history, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(resortList[position])
    }

    override fun getItemCount(): Int = resortList.size
}
