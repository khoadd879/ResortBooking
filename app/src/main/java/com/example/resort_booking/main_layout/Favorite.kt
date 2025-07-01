package com.example.resort_booking.main_layout

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.FavouriteAdapter

import com.example.resort_booking.ClassNDataCLass.HotelAdapter
import com.example.resort_booking.HotelDetailActivity
import com.example.resort_booking.R
import com.example.resort_booking.SharedViewModel
import data.FavoriteRequest
import data.FavoriteResponse
import data.FavouriteResponse
import data.FavouriteListData
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
 * Use the [Favorite.newInstance] factory method to
 * create an instance of this fragment.
 */
class Favorite : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouriteAdapter
    private lateinit var apiService: ApiService
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val sharedPref = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        apiService = com.example.resort_booking.ApiClient.create(sharedPref)
        userId = sharedPref.getString("ID_USER", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewFavourite)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Khởi tạo adapter với danh sách rỗng ban đầu
        adapter = FavouriteAdapter(emptyList(),
            onItemClick = { favourite ->
                val intent = Intent(requireContext(), HotelDetailActivity::class.java)
                intent.putExtra("RESORT_ID", favourite.resortId.toString())
                startActivity(intent)
            },
            onFavoriteClick = { favourite ->
                userId?.let { uid ->
                    val body = FavoriteRequest(favourite.resortId, uid)
                    apiService.createFavorite(body).enqueue(object : Callback<FavoriteResponse> {
                        override fun onResponse(
                            call: Call<FavoriteResponse>,
                            response: Response<FavoriteResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                                sharedViewModel.triggerUpdate() // Thông báo cập nhật
                            }
                        }
                        override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                            Toast.makeText(context, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        )
        recyclerView.adapter = adapter

        // Quan sát sự thay đổi từ ViewModel
        sharedViewModel.favoritesUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                loadFavorites()
                sharedViewModel.resetUpdate()
            }
        }

        // Tải dữ liệu ban đầu
        loadFavorites()
    }

    private fun loadFavorites() {
        userId?.let { uid ->
            apiService.getListFavourite(uid).enqueue(object : Callback<FavouriteResponse> {
                override fun onResponse(
                    call: Call<FavouriteResponse>,
                    response: Response<FavouriteResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.data?.let { favorites ->
                            adapter.updateData(favorites) // Cập nhật dữ liệu mới
                        }
                    }
                }
                override fun onFailure(call: Call<FavouriteResponse>, t: Throwable) {
                    Toast.makeText(context, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(context, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }
}