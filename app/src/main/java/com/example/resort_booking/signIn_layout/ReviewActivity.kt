package com.example.resort_booking.signIn_layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.ClassNDataCLass.Review
import com.example.resort_booking.ClassNDataCLass.ReviewAdapter
import com.example.resort_booking.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reviewList = listOf(
            Review("Nguyen Van A", "https://i.imgur.com/avatar1.jpg", 4.5f, "Khách sạn sạch sẽ, nhân viên thân thiện."),
            Review("Tran Thi B", "https://i.imgur.com/avatar2.jpg", 5f, "Tuyệt vời, gần trung tâm."),
            Review("Le Van C", "https://i.imgur.com/avatar3.jpg", 3.5f, "Ổn, nhưng phòng hơi nhỏ.")
        )

        val adapter = ReviewAdapter(reviewList)
        binding.recyclerViewReview.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReview.adapter = adapter
    }
}
