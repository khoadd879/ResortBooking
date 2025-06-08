package com.example.resort_booking.main_layout

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.ResortUserActivity
import com.example.resort_booking.ClassNDataCLass.FavouriteAdapter
import com.example.resort_booking.ClassNDataCLass.HotelAdapter
import com.example.resort_booking.ClassNDataCLass.HotelRecommendAdapter
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.R
import data.FavoriteRequest
import data.FavoriteResponse
import data.FavouriteResponse
import data.ListUserResponse
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.*


class Homepage : Fragment() {

    private lateinit var recyclerBestResort: RecyclerView
    private lateinit var recyclerRecommendResort: RecyclerView
    private lateinit var recyclerFavoriteResort: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View

    private var pendingRequests = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,

        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Khởi tạo thành phần cho loading
        progressBar = view.findViewById(R.id.progressBar)
        contentLayout = view.findViewById(R.id.contentLayout)


        recyclerRecommendResort = view.findViewById(R.id.recyclerResortsRecommend)
        recyclerBestResort = view.findViewById(R.id.recyclerResortsBest)
        recyclerFavoriteResort = view.findViewById(R.id.recyclerResortsFavorite)

        recyclerBestResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecommendResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerFavoriteResort.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)

        if (userId.isNullOrEmpty()) {
            showToast("Chưa đăng nhập hoặc thiếu thông tin người dùng")
            return
        }

        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        refreshData(apiService, userId)

        val seeAll = view.findViewById<TextView>(R.id.textViewAll)
        seeAll.setOnClickListener {
            val intent = Intent(requireContext(), ResortUserActivity::class.java)
            intent.putExtra("ID_USER", userId)
            startActivity(intent)
        }

    }
    //show progress bar
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE  // Ẩn layout
        } else {
            progressBar.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE   // Hiện layout
        }
    }



    override fun onResume() {
        super.onResume()
        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null) ?: return
        val apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        refreshData(apiService, userId)
    }

    private fun refreshData(apiService: ApiService, userId: String) {
        pendingRequests = 2
        loadAvatar(apiService, userId)
        fetchResortList(apiService, userId)
        fetchFavouriteList(apiService, userId)
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchResortList(apiService: ApiService, userId: String) {
        showLoading(true)
        apiService.getResortList(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val resortList = response.body()?.data ?: emptyList()
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
                                            showToast("Đã thêm vào yêu thích")
                                            refreshData(apiService, userId)
                                        } else {
                                            showToast("Thêm yêu thích thất bại: ${response.code()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                                            showLoading(false)
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
                    showToast("Lỗi response: ${response.code()}")
                }

            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                showToast("Lỗi khi kết nối server: ${t.message}")

            }
        })
    }

    private fun fetchFavouriteList(apiService: ApiService, userId: String) {
        apiService.getListFavourite(userId).enqueue(object : Callback<FavouriteResponse> {
            override fun onResponse(call: Call<FavouriteResponse>, response: Response<FavouriteResponse>) {
                if (response.isSuccessful) {
                    val favouriteList = response.body()?.data?.take(4) ?: emptyList()

                    recyclerFavoriteResort.adapter = FavouriteAdapter(
                        favouriteList,
                        onItemClick = { fav ->
                            val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                            intent.putExtra("RESORT_ID", fav.resortId.toString())
                            startActivity(intent)
                        },
                        onFavoriteClick = { fav ->

                            apiService.deleteFavorite(userId, fav.resortId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {

                                        if (response.isSuccessful) {
                                            showToast("Đã xóa khỏi yêu thích")
                                            fetchFavouriteList(apiService, userId)
                                        } else {
                                            showToast("Xóa yêu thích thất bại: ${response.code()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {

                                        showToast("Lỗi khi kết nối server: ${t.message}")
                                    }
                                })
                        }
                    )
                } else {
                    showToast("Lỗi response: ${response.code()}")
                }

            }

            override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                showToast("Lỗi khi kết nối server: ${t.message}")

            }
        })
    }

    private fun loadAvatar(apiService: ApiService, userId: String) {
        apiService.getListUser().enqueue(object : Callback<ListUserResponse> {
            override fun onResponse(
                call: Call<ListUserResponse?>,
                response: Response<ListUserResponse?>
            ) {
                if (response.isSuccessful) {
                    val listUser = response.body()?.data
                    val user = listUser?.find { it.idUser == userId }

                    val nameuser = view?.findViewById<TextView>(R.id.text_yourName)


                    nameuser?.text = user?.nameuser

                    if (user != null) {
                        val avatarUrl = user.avatar
                        if (!avatarUrl.isNullOrEmpty()) {
                            val imageView = view?.findViewById<ImageView>(R.id.imageAvatar)
                            Glide.with(requireContext())
                                .load(avatarUrl)
                                .error(R.drawable.load_error)
                                .placeholder(R.drawable.load_error)
                                .into(imageView!!)

                        } else {
                            showToast("Người dùng chưa có avatar.")
                        }
                    } else {
                        showToast("Không tìm thấy người dùng.")
                    }
                } else {
                    showToast("Lỗi phản hồi khi lấy danh sách user: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ListUserResponse?>, t: Throwable) {
                showToast("Lỗi kết nối khi lấy danh sách user: ${t.message}")
            }
        })
    }
}
