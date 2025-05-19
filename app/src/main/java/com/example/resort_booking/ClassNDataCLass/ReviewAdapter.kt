package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.R

class ReviewAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgReviewerAvatar)
        val tvName: TextView = itemView.findViewById(R.id.ReviewerName)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ReviewerStar)
        val tvContent: TextView = itemView.findViewById(R.id.ReviewContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvName.text = review.reviewerName
        holder.ratingBar.rating = review.rating
        holder.tvContent.text = review.content
        Glide.with(holder.itemView.context)
            .load(review.reviewerAvatarUrl)
            .circleCrop()
            .into(holder.imgAvatar)
    }

    override fun getItemCount(): Int = reviews.size
}
