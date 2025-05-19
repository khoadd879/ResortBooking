package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R
import data.Favorite


class FavoriteAdapter(
    private val favoriteList: List<Favorite>
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>(){
    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_history, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteList[position])
    }

    override fun getItemCount(): Int = favoriteList.size
}