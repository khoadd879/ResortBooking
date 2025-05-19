package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.databinding.ItemPaymentHistoryBinding
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class PaymentHistoryAdapter(private var items: List<PaymentHistory>) :
    RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemPaymentHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvRoomId.text = "Phòng #${item.roomId}"
            tvBookingTime.text = "Đặt: ${item.bookingTime.format(formatter)}"
            tvCheckIn.text = "Nhận: ${item.checkInTime.format(formatter)}"
            tvCheckOut.text = "Trả: ${item.checkOutTime.format(formatter)}"
            tvTotalAmount.text = "Tổng tiền: %,d VND".format(item.totalAmount.toInt())
            tvStatus.text = when (item.status) {
                PaymentStatus.CONFIRMED -> "Đã xác nhận"
                PaymentStatus.CANCELED -> "Đã hủy"
                PaymentStatus.PENDING -> "Chờ xác nhận"
            }

            Glide.with(imgAvatar.context)
                .load(item.avatarUrl)
                .into(imgAvatar)
        }
    }

    fun updateData(newItems: List<PaymentHistory>) {
        items = newItems
        notifyDataSetChanged()
    }
}
