package com.example.resort_booking.main_layout

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import java.util.Timer
import java.util.TimerTask
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
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    //start Declare variables for auto-scrolling management
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var currentPosition: Int = Integer.MAX_VALUE / 2
    private lateinit var layoutManager: LinearLayoutManager
    private var originalResortList: List<Resort> = emptyList()
    //end Declare variables for auto-scrolling management

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        progressBar = view.findViewById(R.id.progressBar)
        contentLayout = view.findViewById(R.id.contentLayout)
        textCurrentLocation = view.findViewById(R.id.positionHomePage)

        // Show loading immediately and hide content
        showLoading(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //start Set up layout manager and scroll listener for recyclerBestResort
        recyclerBestResort = view.findViewById(R.id.recyclerResortsBest)
        recyclerRecommendResort = view.findViewById(R.id.recyclerResortsRecommend)
        recyclerFavoriteResort = view.findViewById(R.id.recyclerResortsFavorite)

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerBestResort.layoutManager = layoutManager
        recyclerRecommendResort.layoutManager = LinearLayoutManager(requireContext())
        recyclerFavoriteResort.layoutManager = LinearLayoutManager(requireContext())

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerBestResort)

        recyclerBestResort.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateItemScale()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> stopAutoScrollBanner()
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                        runAutoScrollBanner()
                    }
                }
            }
        })
        //end Set up layout manager and scroll listener for recyclerBestResort

        val userId = getUserId() ?: run {
            showToast("Chưa đăng nhập hoặc thiếu thông tin người dùng")
            showLoading(false)
            return
        }

        // Set up "View All" click listener
        view.findViewById<TextView>(R.id.textViewAll).setOnClickListener {
            val intent = Intent(requireContext(), ResortUserActivity::class.java)
            intent.putExtra("ID_USER", userId)
            startActivity(intent)
        }

        checkLocationPermission(userId)
    }

    //start Stop auto-scrolling timer and update current position
    private fun stopAutoScrollBanner() {
        timerTask?.cancel()
        timer?.cancel()
        timer = null
        timerTask = null
        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
    }
    //end Stop auto-scrolling timer and update current position

    //start Start auto-scrolling timer to scroll recyclerBestResort every 4 seconds
    private fun runAutoScrollBanner() {
        if (timer == null && timerTask == null && originalResortList.isNotEmpty()) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        if (currentPosition >= Integer.MAX_VALUE - 1) {
                            currentPosition = Integer.MAX_VALUE / 2
                            recyclerBestResort.scrollToPosition(currentPosition)
                            recyclerBestResort.smoothScrollBy(5, 0) // Small nudge to trigger snap
                        } else {
                            currentPosition++
                            recyclerBestResort.smoothScrollToPosition(currentPosition)
                        }
                    }
                }
            }
            timer?.schedule(timerTask, 4000, 4000) // Scroll every 4 seconds
        }
    }
    //end Start auto-scrolling timer to scroll recyclerBestResort every 4 seconds

    private fun getUserId(): String? {
        return requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("ID_USER", null)
    }

    private fun checkLocationPermission(userId: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(userId)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation(userId: String) {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isAdded) return

                currentLocation = result.lastLocation
                currentLocation?.let {
                    val addressText = getAddressFromLocation(it)
                    textCurrentLocation.text = addressText
                    refreshDataWithLocation(userId, it)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showLoading(false)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun refreshDataWithLocation(userId: String, location: Location) {
        val apiService = com.example.resort_booking.ApiClient.create(requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE))

        apiService.getResortList(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                if (response.isSuccessful) {
                    val resorts = response.body()?.data.orEmpty()

                    val resortsWithDistance = resorts.mapNotNull { resort ->
                        val targetLoc = getLocationFromAddress(resort.location_rs)
                        if (targetLoc != null) {
                            val distanceKm = currentLocation?.distanceTo(targetLoc)?.div(1000)
                            Pair(resort, distanceKm)
                        } else null
                    }.sortedBy { it.second }

                    val topRated = resorts.sortedByDescending { it.star }.take(4)

                    recyclerBestResort.adapter = HotelAdapter(
                        topRated,
                        {
                            startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                                putExtra("RESORT_ID", it.idRs)
                            })
                        },
                        { resort ->
                            val body = FavoriteRequest(resort.idRs, userId)
                            apiService.createFavorite(body).enqueue(object : Callback<FavoriteResponse> {
                                override fun onResponse(
                                    call: Call<FavoriteResponse>,
                                    response: Response<FavoriteResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        showToast("Đã thêm vào yêu thích")
                                        refreshDataWithLocation(userId, location)
                                    } else showToast("Thêm yêu thích thất bại: ${response.code()}")
                                }

                                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                                    showToast("Lỗi mạng: ${t.message}")
                                }
                            })
                        }
                    )

                    //start Set initial scroll position and trigger snap for centering
                    recyclerBestResort.post {
                        recyclerBestResort.scrollToPosition(currentPosition)
                        recyclerBestResort.smoothScrollBy(5, 0)
                        updateItemScale()
                    }
                    //end Set initial scroll position and trigger snap for centering

                    recyclerRecommendResort.adapter = HotelRecommendAdapter(resortsWithDistance.take(4).map { it.first }) {
                        startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                            putExtra("RESORT_ID", it.idRs)
                        })
                    }

                    //start Start auto-scrolling after data is loaded
                    originalResortList = topRated // Ensure originalResortList is set
                    runAutoScrollBanner()
                    //end Start auto-scrolling after data is loaded

                    fetchFavouriteList(apiService, userId)
                    loadAvatar(apiService, userId)
                } else {
                    showToast("Lỗi response: ${response.code()}")
                }
                showLoading(false)
            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                showLoading(false)
                showToast("Lỗi khi kết nối server: ${t.message}")
            }
        })
    }

    private fun fetchFavouriteList(apiService: ApiService, userId: String) {
        apiService.getListFavourite(userId).enqueue(object : Callback<FavouriteResponse> {
            override fun onResponse(call: Call<FavouriteResponse>, response: Response<FavouriteResponse>) {
                if (response.isSuccessful) {
                    val favourites = response.body()?.data?.take(4).orEmpty()
                    recyclerFavoriteResort.adapter = FavouriteAdapter(favourites, {
                        startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                            putExtra("RESORT_ID", it.resortId.toString())
                        })
                    }, {
                        apiService.deleteFavorite(userId, it.resortId).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    showToast("Đã xóa khỏi yêu thích")
                                    fetchFavouriteList(apiService, userId)
                                } else showToast("Xóa yêu thích thất bại: ${response.code()}")
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                showToast("Lỗi khi kết nối server: ${t.message}")
                            }
                        })
                    })
                } else {
                    showToast("Lỗi response: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                showToast("Lỗi khi kết nối server: ${t.message}")
            }
        })
    }

    private fun loadAvatar(apiService: ApiService, userId: String) {
        apiService.getListUser().enqueue(object : Callback<ListUserResponse> {
            override fun onResponse(call: Call<ListUserResponse>, response: Response<ListUserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.data?.find { it.idUser == userId }
                    view?.findViewById<TextView>(R.id.text_yourName)?.text = user?.nameuser
                    user?.avatar?.let {
                        view?.findViewById<ImageView>(R.id.imageAvatar)?.let { imageView ->
                            if (context != null && isAdded) {
                                Glide.with(requireContext())
                                    .load(it)
                                    .error(R.drawable.load_error)
                                    .placeholder(R.drawable.load_error)
                                    .into(imageView)
                            }
                        }
                    }
                } else {
                    showToast("Lỗi phản hồi khi lấy danh sách user: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ListUserResponse>, t: Throwable) {
                showToast("Lỗi kết nối khi lấy danh sách user: ${t.message}")
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).roundToInt() / factor
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val userId = getUserId() ?: return
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(userId)
            } else {
                showToast("Quyền truy cập vị trí bị từ chối")
                refreshDataWithLocation(userId, Location("").apply {
                    latitude = 10.762622
                    longitude = 106.660172
                })
            }
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

    private fun getAddressFromLocation(location: Location): String {
        return try {
            val geocoder = Geocoder(requireContext())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
            } else {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                addressLine ?: "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
            }
        } catch (e: Exception) {
            "${location.latitude.roundTo(4)}, ${location.longitude.roundTo(4)}"
        }
    }

    private fun updateItemScale() {
        val layoutManager = recyclerBestResort.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val view = layoutManager.findViewByPosition(i) ?: continue
            val centerX = recyclerBestResort.width / 2f
            val itemCenterX = (view.left + view.right) / 2f
            val distanceFromCenter = kotlin.math.abs(centerX - itemCenterX)
            val scale = 1f - (distanceFromCenter / centerX) * 0.2f
            view.scaleX = scale.coerceIn(0.8f, 1.2f) // Phóng to tối đa 1.2x
            view.scaleY = scale.coerceIn(0.8f, 1.2f)
        }
    }

    //start Resume auto-scrolling when fragment becomes visible
    override fun onResume() {
        super.onResume()
        val userId = getUserId() ?: return
        checkLocationPermission(userId)
        runAutoScrollBanner()
    }
    //end Resume auto-scrolling when fragment becomes visible

    //start Stop auto-scrolling when fragment is paused
    override fun onPause() {
        super.onPause()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // Ignore if callback wasn't registered
        }
        stopAutoScrollBanner()
    }
    //end Stop auto-scrolling when fragment is paused
}