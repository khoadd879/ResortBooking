package com.example.resort_booking.main_layout

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.ResortUserActivity
import com.example.resort_booking.ClassNDataCLass.FavouriteAdapter
import com.example.resort_booking.ClassNDataCLass.HotelAdapter
import com.example.resort_booking.ClassNDataCLass.HotelRecommendAdapter
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.R
import com.google.android.gms.location.*
import data.*
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class Homepage : Fragment() {

    private lateinit var recyclerBestResort: RecyclerView
    private lateinit var recyclerRecommendResort: RecyclerView
    private lateinit var recyclerFavoriteResort: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View
    private lateinit var textCurrentLocation: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: Location? = null
    private lateinit var layoutManager: LinearLayoutManager
    private var currentPosition = Integer.MAX_VALUE / 2
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var originalResortList: List<Resort> = emptyList()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        contentLayout = view.findViewById(R.id.contentLayout)
        textCurrentLocation = view.findViewById(R.id.positionHomePage)

        recyclerBestResort = view.findViewById(R.id.recyclerResortsBest)
        recyclerRecommendResort = view.findViewById(R.id.recyclerResortsRecommend)
        recyclerFavoriteResort = view.findViewById(R.id.recyclerResortsFavorite)

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerBestResort.layoutManager = layoutManager
        recyclerRecommendResort.layoutManager = LinearLayoutManager(requireContext())
        recyclerFavoriteResort.layoutManager = LinearLayoutManager(requireContext())

        LinearSnapHelper().attachToRecyclerView(recyclerBestResort)

        recyclerBestResort.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                updateItemScale()
            }

            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> stopAutoScrollBanner()
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                        runAutoScrollBanner()
                    }
                }
            }
        })

        view.findViewById<TextView>(R.id.textViewAll).setOnClickListener {
            getUserId()?.let {
                startActivity(Intent(requireContext(), ResortUserActivity::class.java).apply {
                    putExtra("ID_USER", it)
                })
            }
        }

        showLoading(true)

        val userId = getUserId()
        if (userId == null) {
            showToast("Chưa đăng nhập hoặc thiếu thông tin người dùng")
            showLoading(false)
        } else {
            checkLocationPermission(userId)
        }
    }

    private fun checkLocationPermission(userId: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(userId)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation(userId: String) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                textCurrentLocation.text = getAddressFromLocation(location)
                fetchAllData(userId, location)
            } else {
                val fallback = Location("").apply {
                    latitude = 10.762622
                    longitude = 106.660172
                }
                currentLocation = fallback
                textCurrentLocation.text = getAddressFromLocation(fallback)
                fetchAllData(userId, fallback)
            }
        }.addOnFailureListener {
            showToast("Không thể lấy vị trí: ${it.message}")
            showLoading(false)
        }
    }

    private fun fetchAllData(userId: String, location: Location) {
        val apiService = com.example.resort_booking.ApiClient.create(
            requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        )

        var doneCount = 0

        fun checkAllDone() {
            doneCount++
            if (doneCount == 3) showLoading(false)
        }

        apiService.getResortList(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                if (response.isSuccessful) {
                    handleResortData(response.body()?.data.orEmpty(), location, apiService, userId)
                } else showToast("Lỗi resort: ${response.code()}")
                checkAllDone()
            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                showToast("Lỗi resort: ${t.message}")
                checkAllDone()
            }
        })

        apiService.getListFavourite(userId).enqueue(object : Callback<FavouriteResponse> {
            override fun onResponse(call: Call<FavouriteResponse>, response: Response<FavouriteResponse>) {
                if (response.isSuccessful) {
                    handleFavoriteData(response.body()?.data.orEmpty(), apiService, userId)
                } else showToast("Lỗi favorite: ${response.code()}")
                checkAllDone()
            }

            override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                showToast("Lỗi favorite: ${t.message}")
                checkAllDone()
            }
        })

        apiService.getListUser().enqueue(object : Callback<ListUserResponse> {
            override fun onResponse(call: Call<ListUserResponse>, response: Response<ListUserResponse>) {
                if (response.isSuccessful) {
                    handleUserData(response.body()?.data.orEmpty(), userId)
                } else showToast("Lỗi user: ${response.code()}")
                checkAllDone()
            }

            override fun onFailure(call: Call<ListUserResponse>, t: Throwable) {
                showToast("Lỗi user: ${t.message}")
                checkAllDone()
            }
        })
    }

    private fun handleResortData(resorts: List<Resort>, location: Location, api: ApiService, userId: String) {
        val resortsWithDistance = resorts.mapNotNull { resort ->
            getLocationFromAddress(resort.location_rs)?.let {
                val distance = location.distanceTo(it) / 1000
                Pair(resort, distance)
            }
        }.sortedBy { it.second }

        val topRated = resorts.sortedByDescending { it.star }.take(4)

        recyclerBestResort.adapter = HotelAdapter(topRated, onItemClick = {
            startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                putExtra("RESORT_ID", it.idRs)
            })
        }, onFavoriteClick = { resort ->
            val request = FavoriteRequest(resort.idRs, userId)
            api.createFavorite(request).enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                    showToast(if (response.body()?.data == true) "Đã thêm vào yêu thích" else "Đã xóa khỏi yêu thích")
                    fetchAllData(userId, location)
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    showToast("Lỗi: ${t.message}")
                }
            })
        })

        recyclerRecommendResort.adapter = HotelRecommendAdapter(
            resortsWithDistance.take(4).map { it.first }
        ) {
            startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                putExtra("RESORT_ID", it.idRs)
            })
        }

        originalResortList = topRated
        recyclerBestResort.post {
            recyclerBestResort.scrollToPosition(currentPosition)
            recyclerBestResort.smoothScrollBy(5, 0)
            updateItemScale()
        }
        runAutoScrollBanner()
    }

    private fun handleFavoriteData(favourites: List<FavouriteListData>, api: ApiService, userId: String) {
        recyclerFavoriteResort.adapter = FavouriteAdapter(
            favourites,
            onItemClick = { favourite ->
                startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                    putExtra("RESORT_ID", favourite.resortId)
                })
            },
            onFavoriteClick = { favourite ->
                val request = FavoriteRequest(favourite.resortId, userId)
                api.createFavorite(request).enqueue(object : Callback<FavoriteResponse> {
                    override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                        showToast(if (response.body()?.data == true) "Đã thêm vào yêu thích" else "Đã xóa khỏi yêu thích")
                        fetchAllData(userId, currentLocation ?: Location(""))
                    }

                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                        showToast("Lỗi: ${t.message}")
                    }
                })
            }
        )
    }

    private fun handleUserData(users: List<User>, userId: String) {
        val user = users.find { it.idUser == userId }
        view?.findViewById<TextView>(R.id.text_yourName)?.text = user?.nameuser
        user?.avatar?.let {
            if (isAdded && context != null) {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.load_error)
                    .error(R.drawable.load_error)
                    .into(view?.findViewById(R.id.imageAvatar) ?: return)
            }
        }
    }

    private fun getUserId(): String? {
        return requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            .getString("ID_USER", null)
    }

    private fun stopAutoScrollBanner() {
        timerTask?.cancel()
        timer?.cancel()
        timer = null
        timerTask = null
        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
    }

    private fun runAutoScrollBanner() {
        if (timer == null && timerTask == null && originalResortList.isNotEmpty()) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        if (currentPosition >= Integer.MAX_VALUE - 1) {
                            currentPosition = Integer.MAX_VALUE / 2
                            recyclerBestResort.scrollToPosition(currentPosition)
                            recyclerBestResort.smoothScrollBy(5, 0)
                        } else {
                            currentPosition++
                            recyclerBestResort.smoothScrollToPosition(currentPosition)
                        }
                    }
                }
            }
            timer?.schedule(timerTask, 4000, 4000)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getAddressFromLocation(location: Location): String {
        return try {
            val geocoder = Geocoder(requireContext())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
            } else {
                addresses[0].getAddressLine(0)
                    ?: "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
            }
        } catch (e: Exception) {
            "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
        }
    }

    private fun getLocationFromAddress(address: String): Location? {
        return try {
            val geocoder = Geocoder(requireContext())
            val results = geocoder.getFromLocationName(address, 1)
            if (results.isNullOrEmpty()) null else Location("").apply {
                latitude = results[0].latitude
                longitude = results[0].longitude
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).roundToInt() / factor
    }

    private fun updateItemScale() {
        val layoutManager = recyclerBestResort.layoutManager as? LinearLayoutManager ?: return
        val centerX = recyclerBestResort.width / 2f
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        for (i in firstVisible..lastVisible) {
            val view = layoutManager.findViewByPosition(i) ?: continue
            val itemCenter = (view.left + view.right) / 2f
            val distance = abs(centerX - itemCenter)
            val scale = 1f - (distance / centerX) * 0.2f
            view.scaleX = scale.coerceIn(0.8f, 1.2f)
            view.scaleY = scale.coerceIn(0.8f, 1.2f)
        }
    }

    override fun onResume() {
        super.onResume()
        getUserId()?.let { checkLocationPermission(it) }
        runAutoScrollBanner()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScrollBanner()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val userId = getUserId() ?: return
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(userId)
            } else {
                showToast("Quyền vị trí bị từ chối")
                val fallback = Location("").apply {
                    latitude = 10.762622
                    longitude = 106.660172
                }
                currentLocation = fallback
                fetchAllData(userId, fallback)
            }
        }
    }
}

