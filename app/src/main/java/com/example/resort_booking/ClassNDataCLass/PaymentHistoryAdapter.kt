package com.example.resort_booking.ClassNDataCLass

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.BookingDetailActivity
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ItemPaymentHistoryBinding
import data.DataBookingRoom
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PaymentHistoryAdapter(
    private var items: List<DataBookingRoom>,
    private val onDeleteSuccess: () -> Unit // callback để reload sau khi xóa
) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPaymentHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val txtHotelName: TextView = binding.txtHotelName
        val txtLocation: TextView = binding.txtLocation
        val txtPrice: TextView = binding.txtPrice
        val txtDatesIn: TextView = binding.txtDatesIn
        val txtDatesOut: TextView = binding.txtDatesOut
        val txtStatus: TextView = binding.txtStatus
        val imgHotel: ImageView = binding.imgHotel
        val btnDelete: ImageButton = binding.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookingRoom = items[position]

        holder.txtHotelName.text = "Tên: ${bookingRoom.resortResponse.name_rs}"
        holder.txtLocation.text = "Địa chỉ: ${bookingRoom.resortResponse.location_rs}"
        holder.txtPrice.text = "Tổng tiền: ${bookingRoom.total_amount}"
        holder.txtStatus.text = "Trạng thái: ${bookingRoom.status}"

        // Format ngày
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

        try {
            val checkinDate = org.threeten.bp.LocalDateTime.parse(bookingRoom.checkinday, inputFormatter)
            holder.txtDatesIn.text = "Ngày checkin: ${outputFormatter.format(checkinDate)}"
        } catch (e: Exception) {
            holder.txtDatesIn.text = "Ngày checkin: ${bookingRoom.checkinday}"
        }

        try {
            val checkoutDate = org.threeten.bp.LocalDateTime.parse(bookingRoom.checkoutday, inputFormatter)
            holder.txtDatesOut.text = "Ngày checkout: ${outputFormatter.format(checkoutDate)}"
        } catch (e: Exception) {
            holder.txtDatesOut.text = "Ngày checkout: ${bookingRoom.checkoutday}"
        }

        // Load ảnh
        Glide.with(holder.imgHotel.context)
            .load(bookingRoom.resortResponse.image)
            .error(R.drawable.load_error)
            .placeholder(R.drawable.load_error)
            .into(holder.imgHotel)

        // Nhấn để xem chi tiết
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookingDetailActivity::class.java)
            intent.putExtra("BOOKING_ID", bookingRoom.idBr)
            holder.itemView.context.startActivity(intent)
        }

        val sharedPref = holder.itemView.context.getSharedPreferences("APP_PREFS", 0)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val role = sharedPref.getString("ROLE", "")

        // Ẩn nút xóa nếu là user
        if (role?.contains("ROLE_USER") == true) {
            holder.btnDelete.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener {
            apiService.deleteBookingRoom(bookingRoom.idBr).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("PaymentHistoryAdapter", "Xóa thành công")
                        onDeleteSuccess() // Gọi callback để refresh danh sách
                    } else {
                        Log.d("PaymentHistoryAdapter", "Xóa thất bại: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("PaymentHistoryAdapter", "Xóa thất bại: ${t.message}")
                }
            })
        }
    }
}
