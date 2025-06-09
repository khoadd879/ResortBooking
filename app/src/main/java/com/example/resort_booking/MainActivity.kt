package com.example.resort_booking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.resort_booking.databinding.ActivityMainBinding
import com.example.resort_booking.main_layout.Explore
import com.example.resort_booking.main_layout.Favorite
import com.example.resort_booking.main_layout.Homepage
import com.example.resort_booking.main_layout.Profile
import com.example.resort_booking.AdminLayout.ManagementFragment
import com.example.resort_booking.AdminLayout.ReportActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy role từ SharedPreferences
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", "ROLE_USER")
        ApiClient.create(sharedPref)

        val navView = binding.bottomNavigationView
        navView.menu.clear()
        if (role?.contains("ROLE_ADMIN") == true || role?.contains("ROLE_MANAGER") == true) {
            navView.inflateMenu(R.menu.menu_admin)
        } else {
            navView.inflateMenu(R.menu.menu)
        }

        replaceFragment(Homepage())

        // Bottom navigation bar listener
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homepage -> replaceFragment(Homepage())
                R.id.explore -> replaceFragment(Explore())
                R.id.favorite -> replaceFragment(Favorite())
                R.id.managementFragment -> replaceFragment(ManagementFragment())
                R.id.reportActivity -> {
                    val intent = Intent(this, ReportActivity::class.java)
                    startActivity(intent)
                }
                R.id.profile -> replaceFragment(Profile())
            }
            true
        }

    }

    // Function to change fragments
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}
