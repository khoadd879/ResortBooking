package com.example.resort_booking.signIn_layout

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.ServiceAdapter
import com.example.resort_booking.ClassNDataCLass.ServiceModel
import com.example.resort_booking.R

class ServiceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceAdapter
    private val serviceList = mutableListOf<ServiceModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        recyclerView = findViewById(R.id.recyclerService)

        // Dữ liệu mẫu
        serviceList.add(ServiceModel("Giặt ủi", "Giặt đồ sạch sẽ, thơm tho", 50000))
        serviceList.add(ServiceModel("Dọn phòng", "Dọn dẹp phòng hàng ngày", 30000))
        serviceList.add(ServiceModel("Ăn sáng", "Buffet sáng tại nhà hàng", 100000))

        adapter = ServiceAdapter(serviceList) { selectedService ->
            Toast.makeText(this, "Đã thêm: ${selectedService.name}", Toast.LENGTH_SHORT).show()
            // TODO: xử lý thêm vào danh sách dịch vụ đã chọn nếu cần
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
