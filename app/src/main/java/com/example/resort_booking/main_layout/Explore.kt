package com.example.resort_booking.main_layout

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.PaymentHistoryAdapter
import com.example.resort_booking.R
import data.GetListBookingRoomResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Explore.newInstance] factory method to
 * create an instance of this fragment.
 */
class Explore : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var recyclerViewExplore: RecyclerView? = null
    private var paymentHistoryAdapter: PaymentHistoryAdapter? = null

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewExplore = view.findViewById(R.id.recyclerViewPaymentHistory)
        recyclerViewExplore?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)

        if (userId.isNullOrEmpty()) {
            showToast("Chưa đăng nhập hoặc thiếu thông tin người dùng")
            return
        }
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        fetchBookingRoomList(apiService, userId)

    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBookingRoomList(apiService: ApiService, userId: String) {
        apiService.getListBookingRoom(userId).enqueue(object: Callback<GetListBookingRoomResponse>{
            override fun onResponse(
                call: Call<GetListBookingRoomResponse>,
                response: Response<GetListBookingRoomResponse>
            ) {
               if(response.isSuccessful && response.body() != null){
                   val bookingRoomList = response.body()?.data?: emptyList()
                       paymentHistoryAdapter = PaymentHistoryAdapter(bookingRoomList)
                       recyclerViewExplore?.adapter = paymentHistoryAdapter
                   } else {
                       Log.e("Explore", "Không có dữ liệu đặt phòng: ${response.code()}")
                       showToast("Không có dữ liệu đặt phòng.")
               }
            }

            override fun onFailure(call: Call<GetListBookingRoomResponse>, t: Throwable) {
                Log.e("Explore", "Lỗi kết nối: ${t.message}", t)
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Explore.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Explore().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}