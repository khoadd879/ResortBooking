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
        textCurrentLocation = view.findViewById(R.id.textCurrentLocation)

        // Show loading immediately and hide content
        showLoading(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        recyclerBestResort = view.findViewById(R.id.recyclerResortsBest)
        recyclerRecommendResort = view.findViewById(R.id.recyclerResortsRecommend)
        recyclerFavoriteResort = view.findViewById(R.id.recyclerResortsFavorite)

        recyclerBestResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecommendResort.layoutManager = LinearLayoutManager(requireContext())
        recyclerFavoriteResort.layoutManager = LinearLayoutManager(requireContext())

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

                    recyclerBestResort.adapter = HotelAdapter(topRated, {
                        startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                            putExtra("RESORT_ID", it.idRs)
                        })
                    }, { resort ->
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
                    })

                    recyclerRecommendResort.adapter = HotelRecommendAdapter(resortsWithDistance.take(4).map { it.first }) {
                        startActivity(Intent(requireContext(), HotelDetailActivity::class.java).apply {
                            putExtra("RESORT_ID", it.idRs)
                        })
                    }

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

    override fun onResume() {
        super.onResume()
        val userId = getUserId() ?: return
        checkLocationPermission(userId)
    }

    override fun onPause() {
        super.onPause()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // Ignore if callback wasn't registered
        }
    }
}