package com.example.resort_booking.signIn_layout

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.CheckOutActivity
import com.example.resort_booking.R
import com.example.resort_booking.ServiceAdapterBooking
import com.google.gson.Gson
import data.CreateBookingRoomRequest
import data.CreateBookingRoomResponse
import data.ServiceBookingRequest
import data.ServiceWithQuantity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BookingRoomActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_SELECT_SERVICE = 1001
    }

    private lateinit var serviceAdapter: ServiceAdapterBooking
    private val selectedServices = mutableListOf<ServiceWithQuantity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking_room)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val resortId = intent.getStringExtra("RESORT_ID")
        val room_id = intent.getStringExtra("ROOM_ID")

        if(room_id == null){
            Toast.makeText(this, "Lỗi khong co room id", Toast.LENGTH_SHORT).show()
            finish()
        }

        val checkinDate = findViewById<TextView>(R.id.textDateCheckIn)
        val checkoutDate = findViewById<TextView>(R.id.textDateCheckout)
        val amountOfDate = findViewById<TextView>(R.id.AmountOfDate)
        val serviceRecyclerView = findViewById<RecyclerView>(R.id.ServiceListBookingRoom)
        val bookingButton = findViewById<Button>(R.id.button)

        serviceAdapter = ServiceAdapterBooking(selectedServices)
        serviceRecyclerView.adapter = serviceAdapter
        serviceRecyclerView.layoutManager = LinearLayoutManager(this)

        // Date picker cho Check-in
        checkinDate.setOnClickListener {
            showDatePicker { selectedDate ->
                checkinDate.text = selectedDate
                updateDays(checkinDate.text.toString(), checkoutDate.text.toString(), amountOfDate)
            }
        }

        // Date picker cho Check-out
        checkoutDate.setOnClickListener {
            showDatePicker { selectedDate ->
                checkoutDate.text = selectedDate
                updateDays(checkinDate.text.toString(), checkoutDate.text.toString(), amountOfDate)
            }
        }


        // Click xem tất cả dịch vụ
        val allServiceTextView = findViewById<TextView>(R.id.AllService)
        allServiceTextView.setOnClickListener {
            val intent = Intent(this, ServiceActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            intent.putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices)) // Truyền dữ liệu hiện tại
            startActivityForResult(intent, REQUEST_CODE_SELECT_SERVICE)
        }



        // Nút đặt phòng
        bookingButton.setOnClickListener {

            val serviceRequests = selectedServices.map {
                ServiceBookingRequest(id_sv = it.id_sv, quantity = it.quantity)
            }

            val checkinDateText = checkinDate.text.toString() // "2025-05-13"
            val checkoutDateText = checkoutDate.text.toString() // "2025-05-15"

            // Thêm phần giờ chuẩn (giờ check-in 14:00:00, check-out 12:00:00)
            val checkinDateTime = "${checkinDateText}T14:00:00"  // "2025-05-13T14:00:00"
            val checkoutDateTime = "${checkoutDateText}T12:00:00" // "2025-05-15T12:00:00"

            val createBookingRequest = CreateBookingRoomRequest(
                id_user = userId.toString(),
                id_room = room_id.toString(),
                checkinday = checkinDateTime,
                checkoutday = checkoutDateTime,
                services = serviceRequests
            )

            Log.d("DEBUG_REQUEST", Gson().toJson(createBookingRequest))

            apiService.createBookingRoom(createBookingRequest).enqueue(object : Callback<CreateBookingRoomResponse> {
                override fun onResponse(
                    call: Call<CreateBookingRoomResponse>,
                    response: Response<CreateBookingRoomResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BookingRoomActivity, "Đặt phòng thành công", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@BookingRoomActivity, CheckOutActivity::class.java)
                        intent.putExtra("checkInDate", checkinDate.text.toString())
                        intent.putExtra("checkOutDate", checkoutDate.text.toString())
                        intent.putExtra("roomId", room_id)
                        intent.putExtra("id_br", response.body()?.data?.idBr)
                        intent.putExtra("ResortId", resortId)
                        intent.putExtra("money", response.body()?.data?.total_amount?.toPlainString())
                        intent.putParcelableArrayListExtra("SELECTED_SERVICES", ArrayList(selectedServices))
                        startActivity(intent)
                    } else {
                        Log.e("BookingRoomActivity", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CreateBookingRoomResponse?>, t: Throwable) {
                    Log.e("BookingRoomActivity", "Error: ${t.message}")
                }
            })
        }
    }

    // Hiển thị DatePicker và trả về ngày chọn
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate)
            },
            year, month, day)

        dialog.show()
    }

    // Tính số ngày thuê
    private fun updateDays(checkin: String, checkout: String, outputTextView: TextView) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val checkinDate = format.parse(checkin)
            val checkoutDate = format.parse(checkout)

            if (checkinDate != null && checkoutDate != null) {
                val diffMillis = checkoutDate.time - checkinDate.time
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
                outputTextView.text = if (diffDays > 0) diffDays.toString() else "0"
            } else {
                outputTextView.text = "0"
            }
        } catch (e: Exception) {
            outputTextView.text = "0"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_SERVICE && resultCode == RESULT_OK) {
            val services = data?.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES")
            Log.d("BookingRoomActivity", "Selected services: $services")
            if (services != null) {
                selectedServices.clear()
                selectedServices.addAll(services)
                serviceAdapter.updateServices(selectedServices)
            }
        }
    }
}
