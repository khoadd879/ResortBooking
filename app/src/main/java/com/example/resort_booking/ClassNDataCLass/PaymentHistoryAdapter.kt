package com.example.resort_booking.ClassNDataCLass

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.BookingDetailActivity
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ItemPaymentHistoryBinding
import data.DataBookingRoom
import data.GetListBookingRoomResponse
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class PaymentHistoryAdapter(private var items: List<DataBookingRoom>) :
    RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemPaymentHistoryBinding) : RecyclerView.ViewHolder(binding.root){
        val txtHotelName: TextView = binding.txtHotelName
        val txtLocation: TextView = binding.txtLocation
        val txtPrice: TextView = binding.txtPrice
        val txtDatesIn: TextView = binding.txtDatesIn
        val txtDatesOut: TextView = binding.txtDatesOut
        val txtStatus: TextView = binding.txtStatus
        val imgHotel: ImageView = binding.imgHotel
        val btnEdit : ImageButton = binding.btnEdit
        val btnDelete : ImageButton = binding.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookingRoom = items[position]
        holder.txtHotelName.text = "Tên: ${bookingRoom.resortResponse.name_rs}"
        holder.txtLocation.text ="Địa chỉ: ${bookingRoom.resortResponse.location_rs}"
        holder.txtPrice.text = "Tổng tiền: ${bookingRoom.total_amount}"
        holder.txtDatesIn.text = "Ngày checkin: ${bookingRoom.checkinday}"
        holder.txtDatesOut.text = "Ngày checkout: ${bookingRoom.checkoutday}"
        holder.txtStatus.text = "Trạng thái: ${bookingRoom.status}"
        Glide.with(holder.imgHotel.context)
            .load(bookingRoom.resortResponse.image)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.load_error)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookingDetailActivity::class.java)
            intent.putExtra("BOOKING_ID", bookingRoom.idBr)
            holder.itemView.context.startActivity(intent)
        }

        val sharedPref = holder.itemView.context.getSharedPreferences("APP_PREFS", 0)
        val role = sharedPref.getString("ROLE", "")

        if(role?.contains("ROLE_USER") == true ){
            holder.btnEdit.visibility = View.GONE
        }


    }
}
