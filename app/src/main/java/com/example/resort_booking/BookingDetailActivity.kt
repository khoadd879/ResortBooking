package com.example.resort_booking

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import data.ServiceWithQuantity
import data.ServiceUpdate
import data.UpdateBookingRoom
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var serviceAdapter: ServiceBookingDetailAdapter
    private val selectedServices = mutableListOf<ServiceWithQuantity>()
    private var idResort: String? = null
    private var bookingId: String? = null

    // === THAY ĐỔI 1: KHAI BÁO BIẾN LƯU TRỮ NGÀY GỐC ===
    // Lưu trữ ngày check-in và check-out gốc từ API (định dạng yyyy-MM-dd)
    private var originalCheckInDate: String? = null
    private var originalCheckOutDate: String? = null
    // === KẾT THÚC THAY ĐỔI 1 ===

    // UI Elements
    private lateinit var tvRoomName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvTyperoom: TextView
    private lateinit var tvStatus: TextView
    private lateinit var imageRoom: ImageView
    private lateinit var checkinday: TextView
    private lateinit var checkoutday: TextView
    private lateinit var total: TextView
    private lateinit var serviceRecyclerView: RecyclerView
    private lateinit var statusSpinner: Spinner
    private var currentBookingStatus: String? = null

    // Activity Result Launcher for getting result from ServiceActivity
    private val updateServiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedServices = result.data?.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES")
            if (updatedServices != null) {
                selectedServices.clear()
                selectedServices.addAll(updatedServices)
                serviceAdapter.notifyDataSetChanged()
                // NOTE: Sau khi cập nhật dịch vụ, bạn có thể muốn tính toán lại tổng tiền và hiển thị
                // Tuy nhiên, logic này nên được thực hiện sau khi nhấn nút "Cập nhật" và nhận phản hồi từ server
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking_detail)

        initializeViews()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentBookingDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bookingId = intent.getStringExtra("BOOKING_ID")
        if (bookingId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Booking ID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = ApiClient.create(sharedPref)

        fetchBookingDetails(apiService)
        setupClickListeners(apiService)
    }

    private fun initializeViews() {
        tvRoomName = findViewById(R.id.tvRoomName)
        tvPrice = findViewById(R.id.tvPrice)
        tvTyperoom = findViewById(R.id.tvTyperoom)
        tvStatus = findViewById(R.id.tvStatus)
        imageRoom = findViewById(R.id.ivAvatar)
        val btnEdit = findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)
        serviceRecyclerView = findViewById(R.id.ServiceListBookingRoom)
        checkinday = findViewById(R.id.DateBookingCheckIn)
        checkoutday = findViewById(R.id.DateBookingCheckout)
        total = findViewById(R.id.total)
        statusSpinner = findViewById(R.id.statusOption)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val role = sharedPref.getString("ROLE", "")
        if (role?.contains("ROLE_USER") == true) {
            statusSpinner.isEnabled = false
        }

        val options = listOf("Chờ xác nhận", "Đã xác nhận", "Hủy")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = adapter

        btnEdit.visibility = View.GONE
        btnDelete.visibility = View.GONE
        tvStatus.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceBookingDetailAdapter(selectedServices)
        serviceRecyclerView.layoutManager = LinearLayoutManager(this)
        serviceRecyclerView.adapter = serviceAdapter
    }

    private fun fetchBookingDetails(apiService: ApiService) {
        apiService.getInfoBookingRoom(bookingId!!).enqueue(object : Callback<GetInfoBookingRoomResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<GetInfoBookingRoomResponse>, response: Response<GetInfoBookingRoomResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    val bookingRoomDetail = response.body()!!.data!!

                    tvRoomName.text = bookingRoomDetail.roomResponse.name_room
                    tvTyperoom.text = bookingRoomDetail.roomResponse.type_room
                    Glide.with(imageRoom.context)
                        .load(bookingRoomDetail.roomResponse.image)
                        .error(R.drawable.load_error)
                        .placeholder(R.drawable.load_error)
                        .into(imageRoom)

                    showAddressOnMap(bookingRoomDetail.resortResponse.location_rs)
                    idResort = bookingRoomDetail.idResort

                    // === THAY ĐỔI 2: LƯU LẠI NGÀY GỐC VÀ FORMAT ĐỂ HIỂN THỊ ===
                    // 1. Lưu lại giá trị ngày gốc từ API
                    originalCheckInDate = bookingRoomDetail.checkinday
                    originalCheckOutDate = bookingRoomDetail.checkoutday

                    // 2. Format ngày để hiển thị cho người dùng (dd/MM/yyyy)
                    checkinday.text = formatDateString(originalCheckInDate)
                    checkoutday.text = formatDateString(originalCheckOutDate)
                    // === KẾT THÚC THAY ĐỔI 2 ===

                    // Định dạng tiền tệ
                    tvPrice.text = formatCurrency(bookingRoomDetail.roomResponse.price)
                    total.text = formatCurrency(bookingRoomDetail.total_amount)

                    // Cập nhật trạng thái cho Spinner
                    currentBookingStatus = bookingRoomDetail.status ?: "Chờ Xác Nhận"
                    val displayStatus = when (currentBookingStatus) {
                        "Đã Xác Nhận" -> "Đã xác nhận"
                        "Hủy" -> "Hủy"
                        "Chờ Xác Nhận", "PENDING" -> "Chờ xác nhận"
                        else -> "Chờ xác nhận"
                    }
                    val adapter = statusSpinner.adapter as ArrayAdapter<String>
                    val spinnerPosition = adapter.getPosition(displayStatus)
                    if (spinnerPosition >= 0) {
                        statusSpinner.setSelection(spinnerPosition)
                    }

                    // Xử lý services
                    val validServices = bookingRoomDetail.services
                        .filter { !it.idService.isNullOrBlank() }
                        .map { sbRoom ->
                            ServiceWithQuantity(
                                id_sv = sbRoom.idService!!,
                                name = sbRoom.nameService ?: "Không rõ",
                                quantity = sbRoom.quantity ?: 0,
                                describe_service = "",
                                price = BigDecimal.ZERO // Giá được tính ở tổng, không cần ở đây
                            )
                        }
                    val invalidServiceCount = bookingRoomDetail.services.size - validServices.size
                    if (invalidServiceCount > 0) {
                        Log.w("BookingDetailActivity", "$invalidServiceCount dịch vụ từ API đã bị loại bỏ do thiếu ID.")
                    }
                    selectedServices.clear()
                    selectedServices.addAll(validServices)
                    serviceAdapter.notifyDataSetChanged()

                } else {
                    Log.e("BookingDetailActivity", "Lỗi khi tải chi tiết booking: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookingDetailActivity, "Lỗi khi tải chi tiết booking", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetInfoBookingRoomResponse>, t: Throwable) {
                Log.e("BookingDetailActivity", "Lỗi kết nối: ${t.message}", t)
                Toast.makeText(this@BookingDetailActivity, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Định dạng chuỗi ngày tháng từ "yyyy-MM-dd" sang "dd/MM/yyyy" để hiển thị
     */
    private fun formatDateString(inputDateStr: String?): String {
        if (inputDateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDateStr)
            date?.let { outputFormat.format(it) } ?: inputDateStr
        } catch (e: Exception) {
            Log.e("BookingDetailActivity", "Lỗi định dạng ngày: $inputDateStr", e)
            inputDateStr // Trả về chuỗi gốc nếu có lỗi
        }
    }

    /**
     * Định dạng số thành tiền tệ Việt Nam (vd: 1.500.000 đ)
     */
    private fun formatCurrency(amount: BigDecimal?): String {
        if (amount == null) return "0 đ"
        val localeVN = Locale("vi", "VN")
        val currencyFormatter = NumberFormat.getCurrencyInstance(localeVN)
        return currencyFormatter.format(amount).replace("₫", "đ").trim()
    }

    private fun setupClickListeners(apiService: ApiService) {
        findViewById<TextView>(R.id.btnUpdateService).setOnClickListener {
            val intent = Intent(this, ServiceActivity::class.java).apply {
                putExtra("RESORT_ID", idResort)
                putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
            }
            updateServiceLauncher.launch(intent)
        }

        findViewById<Button>(R.id.buttonUpdateBookingRoom).setOnClickListener {
            updateBooking(apiService)
        }
    }

    private fun updateBooking(apiService: ApiService) {
        val serviceRequests = selectedServices.map {
            ServiceUpdate(id_sv = it.id_sv, quantity = it.quantity)
        }

        val selectedStatusVietnamese = statusSpinner.selectedItem.toString()
        val apiStatus = when (selectedStatusVietnamese) {
            "Chờ xác nhận" -> "Chờ Xác Nhận"
            "Đã xác nhận" -> "Đã Xác Nhận"
            "Hủy" -> "Hủy"
            else -> currentBookingStatus ?: "Chờ Xác Nhận"
        }

        // === THAY ĐỔI 3: SỬ DỤNG NGÀY GỐC ĐỂ GỬI LÊN API ===
        // Sử dụng các biến đã lưu trữ ngày gốc (định dạng yyyy-MM-dd)
        // để đảm bảo API nhận được định dạng đúng.
        // Sử dụng toán tử elvis (?:) để tránh lỗi NullPointerException nếu giá trị là null.
        val updateRequest = UpdateBookingRoom(
            idBr = bookingId!!,
            checkinday = originalCheckInDate ?: "",
            checkoutday = originalCheckOutDate ?: "",
            services = serviceRequests,
            status = apiStatus
        )
        // === KẾT THÚC THAY ĐỔI 3 ===

        apiService.updateBookingRoom(bookingId!!, updateRequest).enqueue(object : Callback<CreateBookingRoomResponse> {
            override fun onResponse(call: Call<CreateBookingRoomResponse>, response: Response<CreateBookingRoomResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@BookingDetailActivity, "Cập nhật booking thành công", Toast.LENGTH_SHORT).show()
                    // Sau khi cập nhật thành công, gọi lại fetchBookingDetails để làm mới toàn bộ dữ liệu
                    fetchBookingDetails(apiService)
                } else {
                    Log.e("BookingDetailActivity", "Lỗi khi cập nhật: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@BookingDetailActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CreateBookingRoomResponse>, t: Throwable) {
                Log.e("BookingDetailActivity", "Lỗi kết nối khi cập nhật: ${t.message}", t)
                Toast.makeText(this@BookingDetailActivity, "Cập nhật thất bại do lỗi mạng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddressOnMap(addressString: String) {
        if (!::googleMap.isInitialized) return
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(addressString, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(latLng).title("Vị trí resort"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                googleMap.setOnMapClickListener {
                    val gmmIntentUri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${Uri.encode(addressString)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    startActivity(mapIntent)
                }
            } else {
                Toast.makeText(this, "Không tìm thấy địa chỉ trên bản đồ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi dịch vụ Geocoder", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
}