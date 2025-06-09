package com.example.resort_booking.AdminLayout.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.UpdateUserActivity
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ItemUserBinding
import data.User
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserAdapter(
    private val userList: List<User>,
    private val context: Context
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val apiService: ApiService = com.example.resort_booking.ApiClient.create(
        context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    )

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val binding = holder.binding

        // Set user info
        binding.tvFullName.text = "Họ tên: ${user.nameuser ?: ""}"
        binding.tvGender.text = "Giới tính: ${user.sex ?: ""}"
        binding.tvPhone.text = "SĐT: ${user.phone ?: ""}"
        binding.tvDOB.text = "Ngày sinh: ${user.dob ?: ""}"

        // Load avatar
        Glide.with(binding.imgAvatar.context)
            .load(user.avatar)
            .placeholder(R.drawable.load_error)
            .error(R.drawable.hotel)
            .into(binding.imgAvatar)

        // Edit action
        binding.btnEdit.setOnClickListener {
            val intent = Intent(context, UpdateUserActivity::class.java).apply {
                putExtra("id", user.idUser)
                putExtra("name", user.nameuser)
                putExtra("avatar", user.avatar)
                putExtra("phone", user.phone)
                putExtra("dob", user.dob)
                putExtra("idCard", user.identificationCard)
                putExtra("sex", user.sex)
                putExtra("passport", user.passport)
                putExtra("email", user.email)
                putExtra("account", user.account)
                putStringArrayListExtra("role_user", ArrayList(user.role_user ?: emptyList()))
            }
            context.startActivity(intent)
        }

        // Delete action
        binding.btnDelete.setOnClickListener {
            apiService.deleteUser(user.idUser).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Xóa user thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Xóa user thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun getItemCount(): Int = userList.size
}
