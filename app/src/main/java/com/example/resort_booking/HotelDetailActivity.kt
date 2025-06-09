package com.example.resort_booking

import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.resort_booking.AdminLayout.RoomListActivity
import com.example.resort_booking.ClassNDataCLass.ReviewAdapter
import com.example.resort_booking.signIn_layout.BookingRoomActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import data.CreateEvaluateRequest
import data.Evaluate
import data.EvaluateResponse
import data.FavoriteRequest
import data.FavoriteResponse
import data.ResortDetailResponse
import data.UpdateEvaluateRequest
import data.UpdateEvaluateResponse
import java.io.IOException
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HotelDetailActivity : AppCompatActivity(), OnMapReadyCallback, ReviewAdapter.OnReviewActionListener {

    private lateinit var googleMap: GoogleMap
    private var resortid: String? = null
    private var selectedRoomId: String? = null
    private var selectedRoomName: String? = null
    private lateinit var currentEditEvaluate: Evaluate
    private lateinit var dialog: Dialog
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_detail)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        resortid = intent.getStringExtra("RESORT_ID")
        resortid?.let { fetchResortDetail(it) }

        val btnThemPhong = findViewById<Button>(R.id.btnThemPhong)
        btnThemPhong.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            intent.putExtra("RESORT_ID", resortid)
            intent.putExtra("SELECT_MODE", true)
            startActivityForResult(intent, 123)
        }

        val NameRoom = findViewById<TextView>(R.id.RoomName)
        NameRoom.text = intent.getStringExtra("RESORT_NAME") ?: ""

        val btnFavorite = findViewById<ImageButton>(R.id.favorite_button)
        btnFavorite.setOnClickListener {
            val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val apiService = ApiClient.create(sharedPref)
            val userId = sharedPref.getString("ID_USER", null)

            if (userId == null) {
                showToast("Vui lòng đăng nhập để thêm vào yêu thích")
                return@setOnClickListener
            }

            val body = FavoriteRequest(resortid.toString(), userId)
            apiService.createFavorite(body).enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    if (response.isSuccessful) {
                        btnFavorite.setImageResource(R.drawable.baseline_favorite_24)
                        showToast("Đã thêm vào yêu thích")
                    } else {
                        showToast("Thêm yêu thích thất bại: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    showToast("Lỗi mạng: ${t.message}")
                }
            })
        }

        findViewById<TextView>(R.id.RoomName).text = "Chưa chọn phòng"

        val btnBooking = findViewById<Button>(R.id.bookingBtn)
        btnBooking.setOnClickListener {
            if (selectedRoomId.isNullOrEmpty()) {
                showToast("Vui lòng chọn phòng trước khi đặt")
                return@setOnClickListener
            }

            val intent = Intent(this, BookingRoomActivity::class.java)
            intent.putExtra("ROOM_ID", selectedRoomId)
            intent.putExtra("ROOM_NAME", selectedRoomName)
            intent.putExtra("RESORT_ID", resortid)
            startActivity(intent)
        }

        setupReviewSection()
    }

    private fun showAddressOnMap(addressString: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(addressString, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Vị trí khách sạn")
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                googleMap.setOnMapClickListener {
                    val gmmIntentUri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}(Hotel)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            } else {
                Toast.makeText(this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi tìm địa chỉ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEditReview(evaluate: Evaluate) {
        currentEditEvaluate = evaluate
        showEditReviewDialog(evaluate)
    }

    override fun onDeleteReview(evaluate: Evaluate) {
        deleteEvaluate(evaluate)
    }

    private fun fetchResortDetail(idRs: String) {
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = sharedPref.getString("ID_USER", null)
        val apiService = ApiClient.create(sharedPref)

        apiService.getResortById(idRs, userId ?: "")
            .enqueue(object : Callback<ResortDetailResponse> {
                override fun onResponse(
                    call: Call<ResortDetailResponse>,
                    response: Response<ResortDetailResponse>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        val resort = response.body()!!.data
                        resortid = resort.idRs.toString()

                        findViewById<TextView>(R.id.NameHotel).text = resort.name_rs ?: "No name"
                        findViewById<TextView>(R.id.Location).text = resort.location_rs ?: "No location"
                        findViewById<TextView>(R.id.textView16).text = resort.describe_rs ?: "No description"
                        findViewById<TextView>(R.id.ratingStar).text = resort.star.toString()

                        val imageView = findViewById<ImageView>(R.id.imageViewHotel)
                        Glide.with(this@HotelDetailActivity).load(resort.image).into(imageView)
                        showAddressOnMap(resort.location_rs ?: "")

                        resort.evaluates?.let { evaluates ->
                            val recyclerView = findViewById<RecyclerView>(R.id.rating)
                            recyclerView.layoutManager = LinearLayoutManager(this@HotelDetailActivity)
                            reviewAdapter = ReviewAdapter(evaluates)
                            reviewAdapter.setOnReviewActionListener(this@HotelDetailActivity)
                            recyclerView.adapter = reviewAdapter

                            findViewById<TextView>(R.id.ReviewText).visibility = View.VISIBLE
                            findViewById<LinearLayout>(R.id.ratingContainer).visibility = View.VISIBLE
                            findViewById<LinearLayout>(R.id.commentSection).visibility = View.VISIBLE
                            recyclerView.visibility = View.VISIBLE
                        } ?: run {
                            findViewById<TextView>(R.id.ReviewText).visibility = View.GONE
                            findViewById<LinearLayout>(R.id.ratingContainer).visibility = View.GONE
                            findViewById<LinearLayout>(R.id.commentSection).visibility = View.GONE
                            findViewById<RecyclerView>(R.id.rating).visibility = View.GONE
                        }
                    } else {
                        showToast("Không thể tải chi tiết resort.")
                    }
                }

                override fun onFailure(call: Call<ResortDetailResponse>, t: Throwable) {
                    showToast("Lỗi kết nối: ${t.message}")
                }
            })
    }

    private fun setupReviewSection() {
        val sendCommentBtn = findViewById<Button>(R.id.sendCommentBtn)
        val commentEditText = findViewById<EditText>(R.id.commentEditText)
        val userRatingBar = findViewById<RatingBar>(R.id.userRatingBar)

        sendCommentBtn.setOnClickListener {
            val comment = commentEditText.text.toString().trim()
            val rating = userRatingBar.rating.toInt()

            if (rating == 0) {
                showToast("Vui lòng chọn số sao đánh giá")
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                showToast("Vui lòng nhập bình luận")
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            val userId = sharedPref.getString("ID_USER", null)

            if (userId == null) {
                showToast("Vui lòng đăng nhập để đánh giá")
                return@setOnClickListener
            }

            resortid?.let { resortId ->
                val evaluateRequest = CreateEvaluateRequest(
                    id_rs = resortId,
                    id_user = userId,
                    user_comment = comment,
                    star_rating = rating.toDouble()
                )

                val apiService = ApiClient.create(sharedPref)
                apiService.createEvaluate(evaluateRequest).enqueue(object : Callback<EvaluateResponse> {
                    override fun onResponse(call: Call<EvaluateResponse>, response: Response<EvaluateResponse>) {
                        if (response.isSuccessful) {
                            showToast("Đánh giá thành công")
                            commentEditText.text.clear()
                            userRatingBar.rating = 0f
                            fetchResortDetail(resortId)
                        } else {
                            if(response.code() == 409){
                                showToast("Bạn đã đánh giá rồi")
                                return
                            }
                            Log.e("Evaluate", "Lỗi đánh giá: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<EvaluateResponse>, t: Throwable) {
                        showToast("Lỗi kết nối: ${t.message}")
                    }
                })
            } ?: showToast("Không tìm thấy thông tin resort")
        }
    }

    private fun showEditReviewDialog(evaluate: Evaluate) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_review)

        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.white)

        val ratingBar = dialog.findViewById<RatingBar>(R.id.editRatingBar)
        val editText = dialog.findViewById<EditText>(R.id.editCommentText)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveEdit)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelEdit)

        ratingBar.rating = evaluate.star_rating.toFloat()
        editText.setText(evaluate.user_comment)

        btnSave.setOnClickListener {
            val newComment = editText.text.toString().trim()
            val newRating = ratingBar.rating.toDouble()

            if (newComment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newRating == 0.0) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateEvaluate(currentEditEvaluate.idEvaluate, newComment, newRating)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteEvaluate(evaluate: Evaluate){
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = ApiClient.create(sharedPref)

        apiService.deleteEvaluate(evaluate.idEvaluate).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                if(response.isSuccessful){
                    showToast("Xóa đánh giá thành công")
                    resortid?.let { fetchResortDetail(it) }
                }else{
                    Log.e("Evaluate", "Lỗi xóa đánh giá: ${response.code()}")
                    showToast("Xóa thất bại: ${response.code() ?: "Lỗi không xác định"}")
                }
            }

            override fun onFailure(call: Call<Void?>, t: Throwable) {
                showToast("Lỗi kết nối: ${t.message}")
            }
        })
    }

    private fun updateEvaluate(idEvaluate: String, newComment: String, newRating: Double) {
        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val apiService = ApiClient.create(sharedPref)

        val updateRequest = UpdateEvaluateRequest(
            user_comment = newComment,
            star_rating = newRating
        )

        apiService.updateEvaluate(idEvaluate, updateRequest).enqueue(object : Callback<UpdateEvaluateResponse> {
            override fun onResponse(call: Call<UpdateEvaluateResponse>, response: Response<UpdateEvaluateResponse>) {
                if (response.isSuccessful) {
                    showToast("Cập nhật đánh giá thành công")
                    resortid?.let { fetchResortDetail(it) }
                } else {
                    showToast("Cập nhật thất bại: ${response.code() ?: "Lỗi không xác định"}")
                }
            }

            override fun onFailure(call: Call<UpdateEvaluateResponse>, t: Throwable) {
                showToast("Lỗi kết nối: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            selectedRoomName = data.getStringExtra("ROOM_NAME")
            selectedRoomId = data.getStringExtra("ROOM_ID")
            findViewById<TextView>(R.id.RoomName).text = selectedRoomName ?: ""
        }
    }
}