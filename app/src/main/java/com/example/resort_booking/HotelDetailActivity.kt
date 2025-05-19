package com.example.resort_booking

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import retrofit2.Callback
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.AdminLayout.RoomListActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import data.ResortDetailResponse
import java.io.IOException
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Call
import retrofit2.Response


class HotelDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_detail)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnThemPhong = findViewById<Button>(R.id.btnThemPhong)
        btnThemPhong.setOnClickListener {
            val hotelId = 101  // Ví dụ, bạn có thể lấy từ intent hoặc model
            val intent = Intent(this, RoomListActivity::class.java)
            intent.putExtra("hotelId", hotelId)
            startActivity(intent)
        }
        val resortId = intent.getStringExtra("RESORT_ID")
        if (resortId != null) {
            fetchResortDetail(resortId, accessToken)
        }
    }


    private fun showAddressOnMap(addressString: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(addressString, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Vị trí khách sạn")
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                googleMap.setOnMapClickListener {
                    // Mở Google Maps khi click
                    val gmmIntentUri =
                        Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}(Hotel)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

            } else {
                Toast.makeText(this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi tìm địa chỉ", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchResortDetail(idRs: String, accessToken: String?) {

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getResortById(idRs)
            .enqueue(object : Callback<ResortDetailResponse> {
                override fun onResponse(
                    call: Call<ResortDetailResponse>,
                    response: Response<ResortDetailResponse>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        val resort = response.body()!!.data
                        findViewById<TextView>(R.id.NameHotel).text = resort.name_rs ?: "No name"
                        findViewById<TextView>(R.id.Location).text =
                            resort.location_rs ?: "No location"
                        findViewById<TextView>(R.id.textView16).text =
                            resort.describe_rs ?: "No description"
                        val imageView = findViewById<ImageView>(R.id.imageViewHotel)
                        Glide.with(this@HotelDetailActivity).load(resort.image).into(imageView)
                        showAddressOnMap(resort.location_rs ?: "")
                    } else {
                        showToast("Không thể tải chi tiết resort.")
                    }
                }

                override fun onFailure(call: Call<ResortDetailResponse>, t: Throwable) {
                    showToast("Lỗi kết nối: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

    }
}



