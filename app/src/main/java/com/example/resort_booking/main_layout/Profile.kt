package com.example.resort_booking.main_layout

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.UpdateUserActivity
import com.example.resort_booking.R
import com.example.resort_booking.signIn_layout.HistoryTransactionActivity
import com.example.resort_booking.signIn_layout.LoginActivity
import data.RefreshTokenRequest
import data.UserResponse
import data.UserResponseData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : Fragment() {

    private var currentUser: UserResponseData? = null
    private lateinit var imageAvatar: ImageButton
    private lateinit var name: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textProfile = view.findViewById<TextView>(R.id.textView12)
        val textLogOut = view.findViewById<TextView>(R.id.textLogOut)
        val textHistoryTransaction = view.findViewById<TextView>(R.id.textHistoryTransaction)
        imageAvatar = view.findViewById(R.id.imageViewAvatar)
        name = view.findViewById(R.id.textName)

        fetchUserData()

        textProfile.setOnClickListener {
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Chưa tải xong dữ liệu người dùng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), UpdateUserActivity::class.java).apply {
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
                val roleNames = currentUser?.role_user?.map { it.name } ?: emptyList()
                putStringArrayListExtra("role_user", ArrayList(roleNames))
            }
            startActivity(intent)
        }

        textLogOut.setOnClickListener {
            showLogoutDialog()
        }

        textHistoryTransaction.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryTransactionActivity::class.java))
        }
    }

    private fun fetchUserData() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", AppCompatActivity.MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getUserByID().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()!!.data

                    Glide.with(this@Profile)
                        .load(currentUser?.avatar)
                        .placeholder(R.drawable.load_error)
                        .into(imageAvatar)

                    name.text = currentUser?.nameuser
                } else {
                    Log.e("Profile", "Lỗi lấy thông tin người dùng: ${response.code()}")
                    Toast.makeText(requireContext(), "Lỗi lấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Profile", "Lỗi kết nối: ${t.message}")
            }
        })
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_log_out, null)
        builder.setView(dialogLayout)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnLogout = dialogLayout.findViewById<Button>(R.id.btnLogOut)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)
        val progressBar = dialogLayout.findViewById<ProgressBar>(R.id.progressBar)

        btnLogout.setOnClickListener {
            btnLogout.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val sharedPref = requireContext().getSharedPreferences("APP_PREFS", AppCompatActivity.MODE_PRIVATE)
            val refreshToken = sharedPref.getString("REFRESH_TOKEN", "") ?: ""
            val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

            val request = RefreshTokenRequest(refreshToken)

            apiService.logout(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressBar.visibility = View.GONE
                    clearSessionAndNavigateToLogin()
                    dialog.dismiss()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnLogout.isEnabled = true
                    Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun clearSessionAndNavigateToLogin() {
        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", AppCompatActivity.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
