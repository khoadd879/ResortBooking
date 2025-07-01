package com.example.resort_booking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.resort_booking.AdminLayout.ManagementFragment
import com.example.resort_booking.AdminLayout.ReportActivity
import com.example.resort_booking.databinding.ActivityMainBinding
import com.example.resort_booking.main_layout.Explore
import com.example.resort_booking.main_layout.Favorite
import com.example.resort_booking.main_layout.Homepage
import com.example.resort_booking.main_layout.Profile

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homepageFragment = Homepage()
    private val exploreFragment = Explore()
    private val favoriteFragment = Favorite()
    private val profileFragment = Profile()
    private val managementFragment = ManagementFragment()

    private var activeFragment: Fragment = homepageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupFragments()

        handleIntentNavigation()
    }

    /**
     * Setup Bottom Navigation based on role
     */
    private fun setupBottomNavigation() {
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

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homepage -> switchFragment(homepageFragment)
                R.id.explore -> switchFragment(exploreFragment)
                R.id.favorite -> switchFragment(favoriteFragment)
                R.id.profile -> switchFragment(profileFragment)
                R.id.managementFragment -> switchFragment(managementFragment)
                R.id.reportActivity -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                }
            }
            true
        }
    }

    /**
     * Setup all fragments
     */
    private fun setupFragments() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frameLayout, homepageFragment, "HOME")
        transaction.add(R.id.frameLayout, exploreFragment, "EXPLORE").hide(exploreFragment)
        transaction.add(R.id.frameLayout, favoriteFragment, "FAVORITE").hide(favoriteFragment)
        transaction.add(R.id.frameLayout, profileFragment, "PROFILE").hide(profileFragment)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", "ROLE_USER")
        if (role?.contains("ROLE_ADMIN") == true || role?.contains("ROLE_MANAGER") == true) {
            transaction.add(R.id.frameLayout, managementFragment, "MANAGEMENT").hide(managementFragment)
        }

        transaction.commit()
    }

    /**
     * Switch between fragments
     */
    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(targetFragment)
        }.commit()
        activeFragment = targetFragment
    }

    /**
     * Handle intent to navigate to specific tab
     */
    private fun handleIntentNavigation() {
        val tab = intent.getStringExtra("tab")
        when (tab) {
            "home" -> {
                binding.bottomNavigationView.selectedItemId = R.id.homepage
                switchFragment(homepageFragment)
            }
            "explore" -> {
                binding.bottomNavigationView.selectedItemId = R.id.explore
                switchFragment(exploreFragment)
            }
            "favorite" -> {
                binding.bottomNavigationView.selectedItemId = R.id.favorite
                switchFragment(favoriteFragment)
            }
            "profile" -> {
                binding.bottomNavigationView.selectedItemId = R.id.profile
                switchFragment(profileFragment)
            }
            "management" -> {
                binding.bottomNavigationView.selectedItemId = R.id.managementFragment
                switchFragment(managementFragment)
            }
        }
    }

    /**
     * Để đảm bảo khi quay lại MainActivity vẫn giữ được intent mới
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentNavigation()
    }
}
