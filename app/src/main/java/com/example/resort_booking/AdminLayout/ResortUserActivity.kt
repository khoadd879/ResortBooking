package com.example.resort_booking.AdminLayout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.ResortUserAdapter
import com.example.resort_booking.R
import data.ResortUser

class ResortUserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResortUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resort_user)

        recyclerView = findViewById(R.id.resortRecyclerView)

    }
}
