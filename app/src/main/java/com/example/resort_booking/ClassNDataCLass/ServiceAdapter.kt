package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R

class ServiceAdapter(
    private val serviceList: List<ServiceModel>,
    private val onAddClick: (ServiceModel) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val tvPrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        val btnAdd: ImageButton = itemView.findViewById(R.id.btnAddService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvName.text = service.name
        holder.tvDesc.text = service.description
        holder.tvPrice.text = "${service.price}đ"
        holder.btnAdd.setOnClickListener {
            onAddClick(service)
        }
    }

    override fun getItemCount(): Int = serviceList.size
}
