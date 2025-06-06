package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.databinding.ActivityReportBinding
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import data.MonthlyReportData
import data.ReportListRequest
import data.ReportListResponse
import data.ResortResponse
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var apiService: ApiService

    private val years = (2020..Calendar.getInstance().get(Calendar.YEAR)).toList()
    private val months = (1..12).toList()

    private var resortList = listOf<data.Resort>()
    private var resortId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("ID_USER", null)
        apiService = com.example.resort_booking.ApiClient.create(sharedPref)

        setupSpinners()
        setupChart()
        setupClickEvents()
        loadResorts()
    }

    private fun setupSpinners() {
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        // Mặc định chọn tháng và năm hiện tại
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        binding.spinnerYear.setSelection(years.size - 1)
    }

    private fun setupChart() {
        val chart = binding.lineChart
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.description = Description().apply { text = "Chi tiết tháng" }
        chart.legend.isEnabled = false

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
    }

    private fun setupClickEvents() {
        binding.loadChart.setOnClickListener {
            val selectedYear = binding.spinnerYear.selectedItem as Int
            val selectedMonth = binding.spinnerMonth.selectedItem as Int
            resortId?.let {
                loadChartDataForMonth(selectedMonth, selectedYear)
            }
        }

        binding.btnDetail.setOnClickListener {
            resortId?.let {
                val intent = Intent(this, DetailExpenseActivity::class.java)
                intent.putExtra("RESORT_ID", it)
                startActivity(intent)
            }
        }

        binding.ResortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                resortId = resortList.getOrNull(position)?.idRs
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadResorts() {
        userId?.let { uid ->
            apiService.getResortListCreated(uid).enqueue(object : Callback<ResortResponse> {
                override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                    if (response.isSuccessful) {
                        resortList = response.body()?.data ?: emptyList()
                        if (resortList.isNotEmpty()) {
                            val resortNames = resortList.map { it.name_rs }
                            val adapter = ArrayAdapter(this@ReportActivity, android.R.layout.simple_spinner_item, resortNames)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.ResortSpinner.adapter = adapter

                            resortId = resortList[0].idRs
                        }
                    } else {
                        Log.e("ReportActivity", "Lỗi tải resort: response thất bại")
                    }
                }

                override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                    Log.e("ReportActivity", "Lỗi kết nối khi load resort: ${t.message}")
                }
            })
        }
    }

    private fun loadChartDataForMonth(month: Int, year: Int) {
        val id = resortId ?: return

        val request = ReportListRequest(
            idResort = id,
            reportMonth = month,
            reportYear = year
        )

        apiService.getListReport(request).enqueue(object : Callback<ReportListResponse> {
            override fun onResponse(call: Call<ReportListResponse>, response: Response<ReportListResponse>) {
                if (response.isSuccessful) {
                    val reportData = response.body()?.data
                    if (reportData != null) {
                        // Cập nhật tổng thu, tổng chi, lợi nhuận
                        val totalRevenue = reportData.totalRevenue?.toFloat() ?: 0f
                        val totalExpense = reportData.totalExpense?.toFloat() ?: 0f
                        val netProfit = reportData.netProfit?.toFloat() ?: (totalRevenue - totalExpense)

                        binding.tvTongThu.text = "Tổng thu: %,d VND".format(totalRevenue.toInt())
                        binding.tvTongChi.text = "Tổng chi: %,d VND".format(totalExpense.toInt())
                        binding.tvLoiNhuan.text = "Lợi nhuận: %,d VND".format(netProfit.toInt())

                        // Tạo danh sách dữ liệu biểu đồ từ details
                        val entriesRevenue = ArrayList<Entry>()
                        val entriesExpense = ArrayList<Entry>()

                        reportData.details?.forEachIndexed { index, detail ->
                            when(detail.type) {
                                "Thu" -> entriesRevenue.add(Entry(index.toFloat(), detail.amount.toFloat()))
                                "Chi" -> entriesExpense.add(Entry(index.toFloat(), detail.amount.toFloat()))
                            }
                        }

                        val revenueSet = LineDataSet(entriesRevenue, "Tổng thu").apply {
                            color = Color.parseColor("#4CAF50")
                            setCircleColor(color)
                            lineWidth = 2f
                            circleRadius = 4f
                            valueTextSize = 10f
                        }

                        val expenseSet = LineDataSet(entriesExpense, "Tổng chi").apply {
                            color = Color.parseColor("#F44336")
                            setCircleColor(color)
                            lineWidth = 2f
                            circleRadius = 4f
                            valueTextSize = 10f
                        }

                        binding.lineChart.data = LineData(revenueSet, expenseSet)
                        binding.lineChart.invalidate()
                    }
                } else {
                    Log.e("ReportActivity", "Lỗi API trả về dữ liệu không thành công")
                }
            }

            override fun onFailure(call: Call<ReportListResponse>, t: Throwable) {
                Log.e("ReportActivity", "Lỗi kết nối khi lấy báo cáo: ${t.message}")
            }
        })
    }
}
