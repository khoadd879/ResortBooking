package com.example.resort_booking.main_layout

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.AdminLayout.UpdateUserActivity
import com.example.resort_booking.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Profile : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textProfile = view.findViewById<TextView>(R.id.textView12)
        textProfile.setOnClickListener {
            val intent = android.content.Intent(requireContext(), UpdateUserActivity::class.java)
            startActivity(intent)
        }
        val textLogOut: TextView = view.findViewById(R.id.textLogOut)
        textLogOut.setOnClickListener {
            showLogoutDialog()
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

            apiService.logout(request).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    // Xóa token local bất kể logout thành công hay không
                    if(response.isSuccessful) {
                        progressBar.visibility = View.VISIBLE

                        // Quay lại màn đăng nhập
                        val intent = android.content.Intent(requireContext(), com.example.resort_booking.signIn_layout.LoginActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        dialog.dismiss()
                    }else{
                        progressBar.visibility = View.VISIBLE
                        android.widget.Toast.makeText(requireContext(), "Cant logout: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    progressBar.visibility = View.VISIBLE
                    android.widget.Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            })

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
