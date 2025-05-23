package com.example.resort_booking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import data.CreateCheckOutRequest
import data.CreateCheckOutResponse
import data.Room
import data.RoomResponse
import data.ServiceWithQuantity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class CheckOutActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.checkout)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = ApiClient.create(sharedPref)

        val checkInDate = findViewById<TextView>(R.id.DateBookingCheckIn)
        val checkOutDate = findViewById<TextView>(R.id.DateBookingCheckOut)
        val roomType = findViewById<TextView>(R.id.roomType)
        val totalPrice = findViewById<TextView>(R.id.totalPrice)


        checkInDate.text = intent.getStringExtra("checkInDate")
        checkOutDate.text = intent.getStringExtra("checkOutDate")

        val roomId = intent.getStringExtra("roomId")
        val id_br = intent.getStringExtra("id_br")
        Log.d("CheckOutActivity", "roomId: $roomId, id_br: $id_br")
        val moneyString = intent.getStringExtra("money")
        val moneyBigDecimal = if (moneyString != null) BigDecimal(moneyString) else BigDecimal.ZERO
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(moneyBigDecimal.toDouble())
        totalPrice.text = formattedPrice
        val resortId = intent.getStringExtra("ResortId")


        apiService.getListRoomById(resortId.toString()).enqueue(object: Callback<RoomResponse>{
            override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                if(response.isSuccessful){
                    val roomResponse = response.body()
                    val roomList = roomResponse?.data ?: emptyList()

                    // Tìm phòng theo idRoom
                    val matchedRoom = roomList.find { it.idRoom == roomId }
                    if(matchedRoom != null){
                        roomType.text = matchedRoom.type_room
                    }
                }else{
                    Log.e("CheckOutActivity", "Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                Log.e("CheckOutActivity", "Error: ${t.message}")
            }
        })

        val selectedServices = intent.getParcelableArrayListExtra<ServiceWithQuantity>("SELECTED_SERVICES")

        val selectedServiceRecyclerView = findViewById<RecyclerView>(R.id.ServiceListCheckOut)
        selectedServiceRecyclerView.layoutManager = LinearLayoutManager(this)

        if (selectedServices != null) {
            val adapter = ServiceAdapterBooking(selectedServices)
            selectedServiceRecyclerView.adapter = adapter
        }

        val btnSelectPayment = findViewById<TextView>(R.id.buttonSelectPayment)
        btnSelectPayment.isEnabled = true
        btnSelectPayment.setOnClickListener {
            btnSelectPayment.isEnabled = false
            showDialogPayment { selectedMethod ->
                // Xử lý kết quả trả về từ dialog
               val selectedPayment = selectedMethod
                fetchPayMent(id_br.toString(), selectedPayment)
            }
        }
    }

    private fun showDialogPayment(onPaymentSelected: (String) -> Unit) {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_payment_method, null)

        val checkBoxQr = dialogView.findViewById<CheckBox>(R.id.checkBoxQr)
        val checkBoxMoney = dialogView.findViewById<CheckBox>(R.id.checkBoxMoney)

        // Chọn 1 trong 2 checkbox
        checkBoxQr.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkBoxMoney.isChecked = false
        }

        checkBoxMoney.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkBoxQr.isChecked = false
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val confirmButton = dialogView.findViewById<Button>(R.id.buttonPaymentMethod)
        confirmButton.setOnClickListener {
            when {
                checkBoxQr.isChecked -> {
                   onPaymentSelected("Chuyển Khoản")
                    dialog.dismiss()
                }
                checkBoxMoney.isChecked -> {
                    onPaymentSelected("Tiền Mặt")
                    dialog.dismiss()
                }
                else -> {
                    Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun fetchPayMent(id_br:String, selectedPayment: String){
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = ApiClient.create(sharedPref)

        val createCheckOut = CreateCheckOutRequest(id_br, selectedPayment)
        apiService.createCheckOut(createCheckOut).enqueue(object: Callback<CreateCheckOutResponse>{
            override fun onResponse(
                call: Call<CreateCheckOutResponse>,
                response: Response<CreateCheckOutResponse>
            ) {
                if(response.isSuccessful){
                    Toast.makeText(this@CheckOutActivity, "Thanh toán thành công", Toast.LENGTH_SHORT).show()
                }else{
                    Log.e("CheckOutActivity", "Error: ${response.code()}")
                    Toast.makeText(this@CheckOutActivity, "Thanh toán thất bại", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CreateCheckOutResponse>, t: Throwable) {
                Toast.makeText(this@CheckOutActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}