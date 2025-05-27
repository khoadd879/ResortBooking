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
import java.util.*
import retrofit2.Callback
import retrofit2.Response

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding

    private val timeTypes = listOf("Tháng", "Năm")
    private val years = (2020..Calendar.getInstance().get(Calendar.YEAR)).toList()
    private val months = (1..12).toList()
    private lateinit var apiService: ApiService
    private var userId: String? = null
    private var resortId: String? = null
    private lateinit var monthAdapter: ArrayAdapter<Int>
    private lateinit var yearAdapter: ArrayAdapter<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupChart()


        val btnDetail = binding.btnDetail
        btnDetail.setOnClickListener {
            Log.d("ReportActivity", "btnDetail clicked")
            val intent = Intent(this, DetailExpenseActivity::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        val sharedPref = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", null)

        apiService = com.example.resort_booking.ApiClient.create(getSharedPreferences("APP_PREFS", MODE_PRIVATE))
        loadResort(apiService, userId ?: "")


    }

    private fun setupSpinners() {

        monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        // Set default values

        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        binding.spinnerYear.setSelection(years.size - 1)

        val onChangeListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedYear = binding.spinnerYear.selectedItem as Int
                loadChartDataByYear(selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerMonth.onItemSelectedListener = onChangeListener
        binding.spinnerYear.onItemSelectedListener = onChangeListener
    }

    private fun updateChartWithMonthlyData(data: List<MonthlyReportData>) {
        val sortedData = data.sortedBy { it.month }

        val thuEntries = sortedData.map { Entry(it.month.toFloat(), it.revenue) }
        val chiEntries = sortedData.map { Entry(it.month.toFloat(), it.expense) }

        val thuSet = LineDataSet(thuEntries, "Tổng thu").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        val chiSet = LineDataSet(chiEntries, "Tổng chi").apply {
            color = Color.parseColor("#F44336")
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        binding.lineChart.data = LineData(thuSet, chiSet)
        binding.lineChart.invalidate()

        val tongThu = sortedData.sumOf { it.revenue.toDouble() }
        val tongChi = sortedData.sumOf { it.expense.toDouble() }
        val loiNhuan = tongThu - tongChi

        binding.tvTongThu.text = "Tổng thu: %,d VND".format(tongThu.toInt())
        binding.tvTongChi.text = "Tổng chi: %,d VND".format(tongChi.toInt())
        binding.tvLoiNhuan.text = "Lợi nhuận: %,d VND".format(loiNhuan.toInt())
    }


    private fun setupChart() {
        val chart = binding.lineChart
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.description = Description().apply { text = "Thời gian" }
        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
    }

    private fun loadChartDataByYear(selectedYear: Int) {
        if (resortId == null) return

        val collectedData = mutableListOf<MonthlyReportData>()
        val totalMonths = 12
        var completedCalls = 0

        for (month in 1..totalMonths) {
            val requestBody = ReportListRequest(
                idResort = resortId!!,
                reportMonth = month,
                reportYear = selectedYear
            )

            apiService.getListReport(requestBody).enqueue(object : Callback<ReportListResponse> {
                override fun onResponse(call: Call<ReportListResponse>, response: Response<ReportListResponse>) {
                    completedCalls++
                    if (response.isSuccessful) {
                        val report = response.body()
                        if (report != null) {
                            collectedData.add(
                                MonthlyReportData(
                                    month = month,
                                    revenue = report.totalRevenue.toFloat(),
                                    expense = report.totalExpense.toFloat()
                                )
                            )
                        }
                    }
                    if (completedCalls == totalMonths) {
                        updateChartWithMonthlyData(collectedData)
                    }
                }

                override fun onFailure(call: Call<ReportListResponse>, t: Throwable) {
                    completedCalls++
                    Log.e("ReportActivity", "Failed month $month: ${t.message}")
                    if (completedCalls == totalMonths) {
                        updateChartWithMonthlyData(collectedData)
                    }
                }
            })
        }
    }


    private fun getDaysInMonth(month: Int, year: Int): Int {
        return Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun loadResort(apiService: ApiService, userId: String) {
        apiService.getResortListCreated(userId).enqueue(object : Callback<ResortResponse> {
            override fun onResponse(call: Call<ResortResponse>, response: Response<ResortResponse>) {
                if (response.isSuccessful) {
                    val resorts = response.body()?.data ?: emptyList()

                    // Lưu lại danh sách resort
                    val resortNames = resorts.map { it.name_rs }
                    val adapter = ArrayAdapter(this@ReportActivity, android.R.layout.simple_spinner_item, resortNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.ResortSpinner.adapter = adapter

                    // Gán ID resort khi chọn
                    binding.ResortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            resortId = resorts[position].idRs
                            Log.d("ReportActivity", "Selected resortId: $resortId")
                            val selectedYear = binding.spinnerYear.selectedItem as Int
                            loadChartDataByYear(selectedYear)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
            }

            override fun onFailure(call: Call<ResortResponse>, t: Throwable) {
                Log.e("ReportActivity", "Failed to load resorts: ${t.message}")
            }
        })
    }



}
