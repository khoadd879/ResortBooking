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
import data.Favourite

class FavouriteAdapter(
    private val favouriteList: List<Favourite>,
    private val onItemClick: (Favourite) -> Unit,
    private val onFavoriteClick: (Favourite) -> Unit
) : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    inner class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resortImage: ImageView = itemView.findViewById(R.id.hotelImageRecommend)
        val resortName: TextView = itemView.findViewById(R.id.hotelName)
        val resortLocation: TextView = itemView.findViewById(R.id.hotelLocation)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val favourite = favouriteList[position]

        holder.resortName.text = favourite.name
        holder.resortLocation.text = favourite.location
        holder.ratingText.text = favourite.rating.toString()

        Glide.with(holder.itemView.context)
            .load(favourite.imageUrl)
            .placeholder(R.drawable.hotel)
            .into(holder.resortImage)

        holder.itemView.setOnClickListener {
            onItemClick(favourite)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(favourite)
        }
    }

    override fun getItemCount(): Int = favouriteList.size
}
