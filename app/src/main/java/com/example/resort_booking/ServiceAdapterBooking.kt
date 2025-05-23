package com.example.resort_booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import data.ServiceWithQuantity

class ServiceAdapterBooking(
    private var services: List<ServiceWithQuantity>
) : RecyclerView.Adapter<ServiceAdapterBooking.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textServiceName)
        val amountText: TextView = itemView.findViewById(R.id.textServiceAmount)
    }

    fun updateServices(newServices: List<ServiceWithQuantity>) {
        services = newServices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_booking, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.nameText.text = service.name
        holder.amountText.text = "${service.quantity}"
    }

    override fun getItemCount(): Int = services.size
}
