package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.R
import data.Evaluate

class ReviewAdapter(private val evaluates: List<Evaluate>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    interface OnReviewActionListener {
        fun onEditReview(evaluate: Evaluate)
        fun onDeleteReview(evaluate: Evaluate)
    }

    private var listener: OnReviewActionListener? = null

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgReviewerAvatar)
        val tvName: TextView = itemView.findViewById(R.id.ReviewerName)
        val ratingBar: TextView = itemView.findViewById(R.id.ReviewerStar)
        val tvContent: TextView = itemView.findViewById(R.id.ReviewContent)
        val tvDate: TextView = itemView.findViewById(R.id.tvCreateDate)
        val menuButton: ImageButton = itemView.findViewById(R.id.editMenuBtn)

        private val sharedPref = itemView.context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("ID_USER", null)

        // Removed the idUser from here - it will be handled in onBindViewHolder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val evaluate = evaluates[position]

        // Bind all data here where position is available
        holder.tvName.text = evaluate.nameuser
        holder.ratingBar.text = evaluate.star_rating.toString()
        holder.tvContent.text = evaluate.user_comment
        holder.tvDate.text = evaluate.created_date

        // Handle menu visibility based on current user
        evaluate.idUser?.let { reviewUserId ->
            holder.menuButton.visibility = if (holder.currentUserId == reviewUserId) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } ?: run {
            holder.menuButton.visibility = View.GONE
        }

        // Setup menu button click listener
        holder.menuButton.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.menuButton)
            popup.menuInflater.inflate(R.menu.menu_3_dot, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        listener?.onEditReview(evaluate)
                        true
                    }
                    R.id.menu_delete -> {
                        listener?.onDeleteReview(evaluate)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Load avatar
        Glide.with(holder.itemView.context)
            .load(evaluate.avatar)
            .circleCrop()
            .placeholder(R.drawable.load_error)
            .into(holder.imgAvatar)
    }

    override fun getItemCount(): Int = evaluates.size

    fun setOnReviewActionListener(listener: OnReviewActionListener) {
        this.listener = listener
    }
}