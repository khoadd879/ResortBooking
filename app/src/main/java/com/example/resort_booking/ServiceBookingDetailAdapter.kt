package com.example.resort_booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import data.ServiceBookingRoom
import data.ServiceWithQuantity

class ServiceBookingDetailAdapter(
    private var services: List<ServiceWithQuantity>
): RecyclerView.Adapter<ServiceBookingDetailAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textServiceName)
        val amountText: TextView = itemView.findViewById(R.id.textServiceAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_booking_detail, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.nameText.text = service.name
        holder.amountText.text = "${service.quantity}"
    }

    override fun getItemCount(): Int = services.size
}