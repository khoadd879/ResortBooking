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
import com.example.resort_booking.ClassNDataCLass.HotelRecommendAdapter
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.R
import data.FavoriteRequest
import data.FavoriteResponse
import data.FavouriteResponse
import data.Resort
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class Homepage : Fragment() {

    private lateinit var recyclerBestResort: RecyclerView
    private lateinit var recyclerRecommendResort: RecyclerView
    private lateinit var recyclerFavoriteResort: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerRecommendResort = view.findViewById<RecyclerView>(R.id.recyclerResortsRecommend)
        recyclerBestResort = view.findViewById<RecyclerView>(R.id.recyclerResortsBest)
        recyclerFavoriteResort = view.findViewById<RecyclerView>(R.id.recyclerResortsFavorite)
        recyclerBestResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecommendResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerFavoriteResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "Chưa đăng nhập hoặc thiếu thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        fetchResortList(apiService, userId)
        fetchFavouriteList(apiService, userId)

    }

    override fun onResume() {
        super.onResume()
        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        val userId = sharedPref.getString("ID_USER", null)
        fetchResortList(apiService, userId.toString())
        fetchFavouriteList(apiService, userId.toString())
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Homepage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun fetchResortList(apiService: ApiService, userId: String) {
        apiService.getResortList(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(
                call: Call<ResortResponse>,
                response: Response<ResortResponse>
            ) {
                if (response.isSuccessful) {
                    val resortList: List<Resort> = response.body()?.data ?: emptyList()
                    val top4Resorts = resortList.take(4)

                    recyclerBestResort.adapter = HotelAdapter(
                        resortList = top4Resorts,
                        onItemClick = { resort ->
                            val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                            intent.putExtra("RESORT_ID", resort.idRs)
                            startActivity(intent)
                        },
                        onFavoriteClick = { resort ->
                            val body = FavoriteRequest(resort.idRs, userId)
                            apiService.createFavorite(body)
                                .enqueue(object : Callback<FavoriteResponse> {
                                    override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                                        if (response.isSuccessful) {
                                            showToast("\u0110ã thêm vào yêu thích")
                                            fetchResortList(apiService, userId)
                                            fetchFavouriteList(apiService, userId)
                                        } else {
                                            showToast("Thêm yêu thích thất bại: ${response.code()}")
                                        }
                                    }
                                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                                        showToast("Lỗi mạng: ${t.message}")
                                    }
                                })
                        }
                    )
                    recyclerRecommendResort.adapter = HotelRecommendAdapter(top4Resorts) { resort ->
                        val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                        intent.putExtra("RESORT_ID", resort.idRs)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "Lỗi response: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                showToast("Lỗi khi kết nối server: ${t.message}")
            }
        })
    }

    private fun fetchFavouriteList(apiService: ApiService, userId: String) {
        apiService.getListFavourite(userId).enqueue(object : Callback<FavouriteResponse> {
            override fun onResponse(
                call: Call<FavouriteResponse>,
                response: Response<FavouriteResponse>
            ) {
                if (response.isSuccessful) {
                    val favouriteList = response.body()?.data
                    if (favouriteList != null) {
                        val top4Favourites = favouriteList.take(4) // ✅ Chỉ lấy 4 phần tử đầu tiên

                        recyclerFavoriteResort.adapter = FavouriteAdapter(
                            top4Favourites,
                            onItemClick = { favourite ->
                                val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                                intent.putExtra("RESORT_ID", favourite.resortId.toString())
                                startActivity(intent)
                            },
                            onFavoriteClick = { favourite ->
                                apiService.deleteFavorite(userId, favourite.resortId)
                                    .enqueue(object : Callback<Void> {
                                        override fun onResponse(
                                            call: Call<Void>,
                                            response: Response<Void>
                                        ) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                                                // ✅ Load lại danh sách sau khi xóa thành công
                                                fetchFavouriteList(apiService, userId)
                                            } else {
                                                Toast.makeText(context, "Xóa yêu thích thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Toast.makeText(context, "Lỗi khi kết nối server: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                        )
                    } else {
                        Toast.makeText(context, "Không có dữ liệu yêu thích", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Lỗi response: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi khi kết nối server: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
