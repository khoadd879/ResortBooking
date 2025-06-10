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

    //start Enable infinite scrolling by returning large item count
    override fun getItemCount(): Int = if (resortList.isEmpty()) 0 else Integer.MAX_VALUE
    //end Enable infinite scrolling by returning large item count

    //start Bind data using modulo to cycle through resortList for infinite scrolling
    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(resortList[position % resortList.size]) // Use modulo for infinite loop
    }
    //end Bind data using modulo to cycle through resortList for infinite scrolling

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_history, parent, false)
        return HotelViewHolder(view)
    }

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

            if(resort.favorite == true){
                favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
            }else{
                favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
            }

            itemView.setOnClickListener {
                onItemClick(resort)
            }

            favoriteButton.setOnClickListener {
                onFavoriteClick(resort)
            }
        }
    }
}