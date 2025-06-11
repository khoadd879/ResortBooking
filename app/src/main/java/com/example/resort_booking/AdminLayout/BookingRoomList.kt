package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ApiClient
import com.example.resort_booking.ClassNDataCLass.PaymentHistoryAdapter
import com.example.resort_booking.R
import data.GetListBookingRoomResponse
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingRoomList : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter

    private var resortList = listOf<data.Resort>()
    private var resortId: String? = null
    private var userId: String? = null

    private lateinit var resortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_room_list_of_resort)

        resortSpinner = findViewById(R.id.ResortSpinner)
        recyclerView = findViewById(R.id.recyclerBookingRoomOfResort)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)
        apiService = ApiClient.create(sharedPref)

        loadResorts()
    }

    private fun loadResorts() {
        progressBar.visibility = View.VISIBLE
        userId?.let { uid ->
            apiService.getResortListCreated(uid).enqueue(object : Callback<ResortResponse> {
                override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        resortList = response.body()?.data ?: emptyList()
                        if (resortList.isNotEmpty()) {
                            val resortNames = resortList.map { it.name_rs }
                            val adapter = ArrayAdapter(this@BookingRoomList, android.R.layout.simple_spinner_item, resortNames)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            resortSpinner.adapter = adapter

                            resortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                    resortId = resortList[position].idRs
                                    resortId?.let { fetchBookingRoomList(it) }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }

                            resortId = resortList[0].idRs
                            resortId?.let { fetchBookingRoomList(it) }
                        }
                    } else {
                        Log.e("BookingRoomList", "Lỗi tải danh sách resort: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e("BookingRoomList", "Lỗi kết nối khi load resort: ${t.message}")
                }
            })
        }
    }

    private fun fetchBookingRoomList(resortId: String) {
        progressBar.visibility = View.VISIBLE
        apiService.getBookingsOfResort(resortId).enqueue(object : Callback<GetListBookingRoomResponse> {
            override fun onResponse(call: Call<GetListBookingRoomResponse>, response: Response<GetListBookingRoomResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val bookingRoomList = response.body()?.data ?: emptyList()
                    paymentHistoryAdapter = PaymentHistoryAdapter(bookingRoomList) {
                        fetchBookingRoomList(resortId) // callback khi xóa thành công
                    }
                    recyclerView.adapter = paymentHistoryAdapter
                } else {
                    Log.e("BookingRoomList", "Không có dữ liệu đặt phòng: ${response.code()}")
                    Toast.makeText(this@BookingRoomList, "Không có dữ liệu đặt phòng.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetListBookingRoomResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("BookingRoomList", "Lỗi kết nối: ${t.message}", t)
            }
        })
    }
}
