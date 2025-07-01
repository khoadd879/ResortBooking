package com.example.resort_booking.main_layout

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.FavouriteAdapter

import com.example.resort_booking.ClassNDataCLass.HotelAdapter
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.R
import data.FavoriteRequest
import data.FavoriteResponse
import data.FavouriteResponse
import data.FavouriteListData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Favorite.newInstance] factory method to
 * create an instance of this fragment.
 */
class Favorite : Fragment() {
    // TODO: Rename and change types of parameters
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
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFavourite)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "Chưa đăng nhập hoặc thiếu thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        apiService.getListFavourite(userId).enqueue(object: Callback<FavouriteResponse>{
            override fun onResponse(
                call: Call<FavouriteResponse>,
                response: Response<FavouriteResponse>
            ) {
                if (response.isSuccessful) {
                    val favoriteList = response.body()?.data
                    if (favoriteList != null) {

                        val adapter = FavouriteAdapter(favoriteList,
                            onItemClick = { favourite ->
                                val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                                intent.putExtra("RESORT_ID", favourite.resortId.toString())  // convert nếu cần
                                startActivity(intent)
                            },
                            onFavoriteClick = { favourite ->
                                val body = FavoriteRequest(favourite.resortId, userId)
                                apiService.createFavorite(body).enqueue(object : Callback<FavoriteResponse>{
                                    override fun onResponse(
                                        call: Call<FavoriteResponse>,
                                        response: Response<FavoriteResponse>
                                    ) {
                                        if (response.isSuccessful){
                                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                                        }else{
                                            Toast.makeText(context, "Xóa yêu thích thất bại", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                                        Toast.makeText(context, "Lỗi khi kết nối server: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        )
                        recyclerView.adapter = adapter
                    }else{
                        Toast.makeText(context, "Không có dữ liệu yêu thích", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi khi kết nối server: ${t.message}", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment Favorite.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Favorite().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}