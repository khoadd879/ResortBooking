package com.example.resort_booking.signIn_layout

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resort_booking.ClassNDataCLass.ResortAdapter
import com.example.resort_booking.databinding.ActivitySearchBinding
import data.Resort

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var fullResortList: List<Resort>
    private lateinit var resortAdapter: ResortAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchBar()
        setupRatingFilter()
    }

    private fun setupRecyclerView() {
//        resortAdapter = ResortAdapter(fullResortList)
        binding.recyclerHotel.layoutManager = LinearLayoutManager(this)
        binding.recyclerHotel.adapter = resortAdapter
    }

    private fun setupSearchBar() {
        binding.search.setOnClickListener {
            binding.recyclerHotel.visibility = View.VISIBLE
//            resortAdapter = ResortAdapter(fullResortList)
            binding.recyclerHotel.adapter = resortAdapter
        }
    }

    private fun setupRatingFilter() {
        val radioButtons = listOf(
            binding.rating1, binding.rating2, binding.rating3,
            binding.rating4, binding.rating5
        )

        for (radio in radioButtons) {
            radio.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    val selectedRating = (buttonView as RadioButton).text.toString().toInt()
                    val filteredList = fullResortList.filter { it.star.toInt() == selectedRating }
//                    resortAdapter = ResortAdapter(filteredList)
                    binding.recyclerHotel.adapter = resortAdapter
                }
            }
        }
    }
}
