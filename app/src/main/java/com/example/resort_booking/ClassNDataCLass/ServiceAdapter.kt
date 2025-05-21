package com.example.resort_booking.ClassNDataCLass

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R
import com.example.resort_booking.signIn_layout.BookingRoomActivity
import data.Service
import data.ServiceWithQuantity

class ServiceAdapter(
    private var serviceList: List<Service>,
    private val onAddClick: (ServiceWithQuantity) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val tvPrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        val btnAdd: ImageButton = itemView.findViewById(R.id.btnAddService)
        val btnMinus: ImageButton = itemView.findViewById(R.id.minusBtn)
        val btnPlus: ImageButton = itemView.findViewById(R.id.plusBtn)
        val tvQuantity: EditText = itemView.findViewById(R.id.Quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvName.text = service.nameService
        holder.tvDesc.text = service.describe_sv
        holder.tvPrice.text = "${service.price}đ"
        holder.tvQuantity.setText("0")

        holder.btnPlus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = currentQuantity + 1
            holder.tvQuantity.setText(newQuantity.toString())
        }

        holder.btnMinus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = if (currentQuantity > 0) currentQuantity - 1 else 0
            holder.tvQuantity.setText(newQuantity.toString())
        }

        holder.btnAdd.setOnClickListener {
            val quantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            if (quantity > 0) {
                val selected = ServiceWithQuantity(
                    id_sv = service.idService,
                    name = service.nameService,
                    quantity = quantity
                )
                onAddClick(selected)
            }
        }
    }

    override fun getItemCount(): Int = serviceList.size
}

