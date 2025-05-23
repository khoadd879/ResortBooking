package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.AdminLayout.UpdateServiceActivity
import com.example.resort_booking.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import data.Service
import data.ServiceWithQuantity

class ServiceAdapter(
    private var serviceList: List<Service>,
    private val onAddClick: (ServiceWithQuantity) -> Unit,
    private var context: Context,
    private val role: String?
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val tvPrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        val btnMinus: Button = itemView.findViewById(R.id.minusBtn)
        val btnPlus: Button = itemView.findViewById(R.id.plusBtn)
        val tvQuantity: EditText = itemView.findViewById(R.id.Quantity)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val manage: LinearLayout = itemView.findViewById(R.id.ManageServiceList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvName.text = service.name_sv
        holder.tvDesc.text = service.describe_service
        val formattedPrice = String.format("%,.2fđ", service.price)
        holder.tvPrice.text = formattedPrice
        holder.tvQuantity.setText("0")

        // Ẩn/hiện layout quản lý theo vai trò
        holder.manage.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        // Sửa
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateServiceActivity::class.java)
            intent.putExtra("service_id", service.idService)
            intent.putExtra("service_name", service.name_sv)
            intent.putExtra("service_price", service.price)
            intent.putExtra("service_desc", service.describe_service)
            holder.itemView.context.startActivity(intent)
        }

        // Xóa
        val sharedPref = context.getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        holder.btnDelete.setOnClickListener {
            apiService.deleteService(service.idService).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Xóa dịch vụ thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Xóa thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Cộng dịch vụ
        holder.btnPlus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = currentQuantity + 1
            holder.tvQuantity.setText(newQuantity.toString())
            updateSelection(service, newQuantity)
        }

        // Trừ dịch vụ
        holder.btnMinus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = if (currentQuantity > 0) currentQuantity - 1 else 0
            holder.tvQuantity.setText(newQuantity.toString())
            updateSelection(service, newQuantity)
        }

        // Nếu người dùng nhập số lượng thủ công
        holder.tvQuantity.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val quantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
                updateSelection(service, quantity)
            }
        }
    }

    private fun updateSelection(service: Service, quantity: Int) {
        val selected = ServiceWithQuantity(
            id_sv = service.idService,
            name = service.name_sv,
            quantity = quantity
        )
        onAddClick(selected)
    }

    override fun getItemCount(): Int = serviceList.size
}
