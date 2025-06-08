package com.example.resort_booking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R
import data.Payment

class HistoryTransactionAdapter(private val payments: List<Payment>) :
    RecyclerView.Adapter<HistoryTransactionAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMoney: TextView = itemView.findViewById(R.id.tvMoney)
        val tvCreateDate: TextView = itemView.findViewById(R.id.tvCreateDate)
        val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.tvMoney.text = "Số tiền: ${payment.money} VND"
        holder.tvCreateDate.text = "Ngày: ${payment.create_date}"
        holder.tvPaymentMethod.text = "Phương thức: ${payment.payment_method}"
    }

    override fun getItemCount(): Int = payments.size
}
