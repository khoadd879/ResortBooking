package com.example.resort_booking.AdminLayout.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.UpdateResortActivity
import com.example.resort_booking.AdminLayout.UpdateUserActivity
import com.example.resort_booking.R
import com.example.resort_booking.databinding.ItemUserBinding
import data.User
import interfaceAPI.ApiService
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserAdapter(private val userList: List<User>,
                  private val context: Context,) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        with(holder.binding) {
            tvFullName.text = "Họ tên: ${user.nameuser}"
            tvGender.text = "Giới tính: ${user.sex}"
            tvPhone.text = "SĐT: ${user.phone}"
            tvDOB.text = "Mật khẩu: ${user.dob}"
            Glide.with(imgAvatar.context)
                .load(user.avatar)
                .placeholder(R.drawable.load_error)
                .error(R.drawable.hotel)
                .into(imgAvatar)
        }

        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)


        val btnedit = holder.itemView.findViewById<ImageView>(R.id.btnEdit)

        btnedit.setOnClickListener {
            val intent = Intent(context, UpdateUserActivity::class.java)
            intent.putExtra("id", user.idUser)
            intent.putExtra("name", user.nameuser)
            intent.putExtra("avatar", user.avatar)
            intent.putExtra("phone", user.phone)
            intent.putExtra("dob", user.dob)
            intent.putExtra("idCard", user.identificationCard)
            intent.putExtra("sex", user.sex)
            intent.putExtra("passport", user.passport)
            intent.putExtra("email", user.email)
            intent.putExtra("account", user.account)
            intent.putExtra("role_user", user.role_user)

            context.startActivity(intent)

        }

        val btndelete = holder.itemView.findViewById<ImageView>(R.id.btnDelete)
        btndelete.setOnClickListener {

            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            apiService.deleteUser(user.idUser).enqueue(object: Callback<Void>{
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Xóa user thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Xóa user thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun getItemCount() = userList.size
}