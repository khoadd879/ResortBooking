    package com.example.resort_booking

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.location.Geocoder
    import android.net.Uri
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.Button
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.example.resort_booking.signIn_layout.ServiceActivity
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.MarkerOptions
    import data.CreateBookingRoomResponse
    import data.GetInfoBookingRoomResponse
    import data.ServiceBookingRequest
    import data.ServiceUpdate
    import data.ServiceWithQuantity
    import data.UpdateBookingRoom
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import java.io.IOException
    import java.math.BigDecimal
    import java.util.Locale

    class BookingDetailActivity : AppCompatActivity(), OnMapReadyCallback{

        private lateinit var googleMap: GoogleMap
        private lateinit var serviceAdapter: ServiceAdapterBooking
        private val selectedServices = mutableListOf<ServiceWithQuantity>()
        private var idResort:String ? = null

        //code để gửi cho bên serviceActivity để có thể trả về update service
        companion object {
            const val REQUEST_CODE_UPDATE_SERVICE = 1001
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.booking_detail)

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragmentBookingDetail) as SupportMapFragment
            mapFragment.getMapAsync(this)

            val bookingId = intent.getStringExtra("BOOKING_ID")

            val tvRoomName = findViewById<TextView>(R.id.tvRoomName)
            val tvPrice = findViewById<TextView>(R.id.tvPrice)
            val tvTyperoom = findViewById<TextView>(R.id.tvTyperoom)
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            val imageRoom = findViewById<ImageView>(R.id.ivAvatar)
            val btnEdit = findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = findViewById<ImageButton>(R.id.btnDelete)
            val serviceRecyclerView = findViewById<RecyclerView>(R.id.ServiceListBookingRoom)
            val btnUpdateService = findViewById<TextView>(R.id.btnUpdateService)

            val checkinday = findViewById<TextView>(R.id.DateBookingCheckIn)
            val checkoutday = findViewById<TextView>(R.id.DateBookingCheckout)
            val serviceTotal = findViewById<TextView>(R.id.serviceTotal)
            val total = findViewById<TextView>(R.id.total)

            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
            tvStatus.visibility = View.GONE



            val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            apiService.getInfoBookingRoom(bookingId.toString()).enqueue(object : Callback<GetInfoBookingRoomResponse> {
                @SuppressLint("CheckResult", "SetTextI18n")
                override fun onResponse(
                    call: Call<GetInfoBookingRoomResponse?>,
                    response: Response<GetInfoBookingRoomResponse?>
                ) {
                    if(response.isSuccessful){
                        val bookingRoomDetail = response.body()?.data
                        if(bookingRoomDetail != null) {
                            tvRoomName.text = bookingRoomDetail.roomResponse.name_room
                            tvPrice.text = bookingRoomDetail.roomResponse.price.toString()
                            tvTyperoom.text = bookingRoomDetail.roomResponse.type_room

                            Glide.with(imageRoom.context)
                                .load(bookingRoomDetail.roomResponse.image)
                                .error(R.drawable.load_error)
                                .placeholder(R.drawable.load_error)

                            showAddressOnMap(bookingRoomDetail.resortResponse.location_rs)

                            checkinday.text = bookingRoomDetail.checkinday
                            checkoutday.text = bookingRoomDetail.checkoutday
                            total.text = bookingRoomDetail.total_amount.toString()
                            idResort = bookingRoomDetail.idResort

                            val services = bookingRoomDetail.services
                            if (services != null) {
                                val serviceRecyclerView = findViewById<RecyclerView>(R.id.ServiceListBookingRoom)
                                val adapter = ServiceBookingDetailAdapter(services)
                                serviceRecyclerView.layoutManager = LinearLayoutManager(this@BookingDetailActivity)
                                serviceRecyclerView.adapter = adapter

                                val totalServiceAmount = services.sumOf { it.total_amount ?: BigDecimal.ZERO }
                                serviceTotal.text = totalServiceAmount.toString()
                            }
                        }
                    }
                    else{
                        Log.e("BookingActivity", "Lỗi khi tải room: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<GetInfoBookingRoomResponse?>, t: Throwable) {
                    Log.e("BookingActivity", "Lỗi kết nối: ${t.message}", t)
                }
            })


            btnUpdateService.setOnClickListener {
                val intent = Intent(this, ServiceActivity::class.java)
                intent.putExtra("RESORT_ID", idResort)

                intent.putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
                startActivityForResult(intent, REQUEST_CODE_UPDATE_SERVICE)
            }

            val serviceRequests = selectedServices.map {
                ServiceUpdate(id_sv = it.id_sv, quantity = it.quantity)
            }

            val updateBookingRoomRequest = UpdateBookingRoom(
                idBr = bookingId.toString(),
                checkinday = checkinday.text.toString(),
                checkoutday = checkoutday.text.toString(),
                services = serviceRequests
            )

            val btnUpdate = findViewById<Button>(R.id.buttonUpdateBookingRoom)
            btnUpdate.setOnClickListener {
                apiService.updateBookingRoom(bookingId.toString(), updateBookingRoomRequest).enqueue(object : Callback<CreateBookingRoomResponse>{
                    override fun onResponse(
                        call: Call<CreateBookingRoomResponse?>,
                        response: Response<CreateBookingRoomResponse?>
                    ) {
                        if(response.isSuccessful){
                            Toast.makeText(this@BookingDetailActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.e("BookingActivity", "Lỗi khi tải room: ${response.code()}")
                            Toast.makeText(this@BookingDetailActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateBookingRoomResponse?>, t: Throwable) {
                        Log.e("BookingActivity", "Lỗi kết nối: ${t.message}", t)
                        Toast.makeText(this@BookingDetailActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        // Hiển thị địa chỉ khách sạn trên bản đồ
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
                        val gmmIntentUri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}(Hotel)")
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

        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            googleMap.uiSettings.isZoomControlsEnabled = true

        }

        //override để có thể nhận về service khác khi update
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_CODE_UPDATE_SERVICE && resultCode == RESULT_OK) {
                val updatedServices = data?.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES")
                if (updatedServices != null) {
                    selectedServices.clear()
                    selectedServices.addAll(updatedServices)

                    // Cập nhật lại UI
                    serviceAdapter.notifyDataSetChanged()
                }
            }
        }

    }