package com.example.resort_booking.ClassNDataCLass

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
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
import data.ServiceWithQuantity

class ServiceAdapter(
    private var serviceList: List<ServiceWithQuantity>,
    private val onAddClick: (ServiceWithQuantity) -> Unit,
    private val context: Context,
    private val role: String?
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val btnMinus: Button = itemView.findViewById(R.id.minusBtn)
        val btnPlus: Button = itemView.findViewById(R.id.plusBtn)
        val tvQuantity: EditText = itemView.findViewById(R.id.Quantity)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val manage: LinearLayout = itemView.findViewById(R.id.ManageServiceList)
        var quantityWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvName.text = service.name
        holder.tvDesc.text = service.describe_service

        // Tránh TextWatcher chồng lặp khi view được tái sử dụng
        holder.quantityWatcher?.let { holder.tvQuantity.removeTextChangedListener(it) }
        holder.tvQuantity.setText(service.quantity.toString())

        // Ẩn/hiện phần quản lý dịch vụ theo vai trò
        holder.manage.visibility = if (role?.contains("ROLE_USER") == true) View.GONE else View.VISIBLE

        // Sửa dịch vụ
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateServiceActivity::class.java)
            intent.putExtra("service_id", service.id_sv)
            intent.putExtra("service_name", service.name)
            intent.putExtra("service_price", service.price)
            intent.putExtra("service_desc", service.describe_service)
            holder.itemView.context.startActivity(intent)
        }

        // Xóa dịch vụ (Admin)
        val sharedPref = context.getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        holder.btnDelete.setOnClickListener {
            apiService.deleteService(service.id_sv).enqueue(object : Callback<Void> {
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

        // Cộng số lượng
        holder.btnPlus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = currentQuantity + 1
            service.quantity = newQuantity
            holder.tvQuantity.setText(newQuantity.toString())
            updateSelection(service, newQuantity)
        }

        // Trừ số lượng
        holder.btnMinus.setOnClickListener {
            val currentQuantity = holder.tvQuantity.text.toString().toIntOrNull() ?: 0
            val newQuantity = if (currentQuantity > 0) currentQuantity - 1 else 0
            service.quantity = newQuantity
            holder.tvQuantity.setText(newQuantity.toString())
            updateSelection(service, newQuantity)
        }

        // Khi người dùng nhập tay
        holder.quantityWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val quantity = s.toString().toIntOrNull() ?: 0
                service.quantity = quantity
                updateSelection(service, quantity)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.tvQuantity.addTextChangedListener(holder.quantityWatcher)
    }

    private fun updateSelection(service: ServiceWithQuantity, quantity: Int) {
        val updatedService = service.copy(quantity = quantity)
        onAddClick(updatedService)
    }

    override fun getItemCount(): Int = serviceList.size
}
