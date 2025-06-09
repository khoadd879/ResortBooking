package com.example.resort_booking.main_layout

import android.app.AlertDialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.UpdateUserActivity
import com.example.resort_booking.R
import com.example.resort_booking.signIn_layout.HistoryTransactionActivity
import com.example.resort_booking.signIn_layout.LoginActivity
import data.UserResponse
import data.UserResponseData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : Fragment() {

    private var currentUser: UserResponseData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textProfile = view.findViewById<TextView>(R.id.textView12)
        val textLogOut = view.findViewById<TextView>(R.id.textLogOut)
        val textHistoryTransaction = view.findViewById<TextView>(R.id.textHistoryTransaction)
        val imageAvatar = view.findViewById<ImageButton>(R.id.imageViewAvatar)
        val name = view.findViewById<TextView>(R.id.textName)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", AppCompatActivity.MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getUserByID().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()?.data

                    Glide.with(imageAvatar.context)
                        .load(currentUser?.avatar)
                        .placeholder(R.drawable.load_error)
                        .into(imageAvatar)

                    name.text = currentUser?.nameuser
                } else {
                    Log.e("Profile", "Lỗi lấy thông tin người dùng: ${response.code()}")
                    android.widget.Toast.makeText(requireContext(), "Lỗi lấy thông tin người dùng", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                android.widget.Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                Log.e("Profile", "Lỗi kết nối: ${t.message}")
            }
        })

        textProfile.setOnClickListener {
            if (currentUser == null) {
                android.widget.Toast.makeText(requireContext(), "Chưa tải xong dữ liệu người dùng", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = android.content.Intent(requireContext(), UpdateUserActivity::class.java).apply {
                putExtra("id", currentUser?.idUser)
                putExtra("name", currentUser?.nameuser)
                putExtra("avatar", currentUser?.avatar)
                putExtra("phone", currentUser?.phone)
                putExtra("dob", currentUser?.dob)
                putExtra("idCard", currentUser?.identificationCard)
                putExtra("sex", currentUser?.sex)
                putExtra("passport", currentUser?.passport)
                putExtra("email", currentUser?.email)
                putExtra("account", currentUser?.account)

                // Nếu role_user là List<RoleUser>, chỉ truyền name
                val roleNames = currentUser?.role_user?.map { it.name } ?: emptyList()
                putStringArrayListExtra("role_user", ArrayList(roleNames))
            }

            startActivity(intent)
        }

        textLogOut.setOnClickListener {
            showLogoutDialog()
        }

        textHistoryTransaction.setOnClickListener {
            val intent = android.content.Intent(requireContext(), HistoryTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_log_out, null)

        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnLogout = dialogLayout.findViewById<Button>(R.id.btnLogOut)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)
        val progressBar = dialogLayout.findViewById<ProgressBar>(R.id.progressBar)

        btnLogout.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("APP_PREFS", AppCompatActivity.MODE_PRIVATE)
            val refreshToken = sharedPref.getString("REFRESH_TOKEN", "") ?: ""
            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            val request = data.RefreshTokenRequest(refreshToken)

            apiService.logout(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressBar.visibility = View.VISIBLE
                    val context = this@Profile.context
                    if (context != null) {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        dialog.dismiss()
                    }

                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressBar.visibility = View.VISIBLE
                    android.widget.Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
