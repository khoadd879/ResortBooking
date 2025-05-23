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
import com.example.resort_booking.R
import data.Resort

class ResortUserAdapter(
    private val resorts: List<Resort>,
    private val context: Context,
    private val onResortDeleted: (() -> Unit)? = null
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
            Toast.makeText(it.context, "Yêu thích: ${resort.name_rs}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getItemCount(): Int = resorts.size
}