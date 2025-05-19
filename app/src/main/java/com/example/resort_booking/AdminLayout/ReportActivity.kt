package com.example.resort_booking.AdminLayout

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.resort_booking.databinding.ActivityReportBinding
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding

    private val timeTypes = listOf("Tháng", "Năm")
    private val years = (2020..Calendar.getInstance().get(Calendar.YEAR)).toList()
    private val months = (1..12).toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupChart()
        loadChartData("Tháng", month = 1, year = 2024)
    }

    private fun setupSpinners() {
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeTypes)
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeType.adapter = timeAdapter

        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        // Set default values
        binding.spinnerTimeType.setSelection(0)
        binding.spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        binding.spinnerYear.setSelection(years.size - 1)

        val onChangeListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedTime = binding.spinnerTimeType.selectedItem.toString()
                val selectedMonth = binding.spinnerMonth.selectedItem as Int
                val selectedYear = binding.spinnerYear.selectedItem as Int
                loadChartData(selectedTime, selectedMonth, selectedYear)

                // Ẩn spinner tháng nếu chọn "Năm"
                binding.spinnerMonth.visibility =
                    if (selectedTime == "Năm") View.GONE else View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerTimeType.onItemSelectedListener = onChangeListener
        binding.spinnerMonth.onItemSelectedListener = onChangeListener
        binding.spinnerYear.onItemSelectedListener = onChangeListener
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

    private fun loadChartData(loai: String, month: Int, year: Int) {
        val thuEntries = mutableListOf<Entry>()
        val chiEntries = mutableListOf<Entry>()

        if (loai == "Tháng") {
            val daysInMonth = getDaysInMonth(month, year)
            for (i in 1..daysInMonth) {
                thuEntries.add(Entry(i.toFloat(), (1000..5000).random().toFloat()))
                chiEntries.add(Entry(i.toFloat(), (500..4000).random().toFloat()))
            }
        } else {
            for (i in 1..12) {
                thuEntries.add(Entry(i.toFloat(), (10000..50000).random().toFloat()))
                chiEntries.add(Entry(i.toFloat(), (5000..30000).random().toFloat()))
            }
        }

        val thuSet = LineDataSet(thuEntries, "Thu").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        val chiSet = LineDataSet(chiEntries, "Chi").apply {
            color = Color.parseColor("#F44336")
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        val lineData = LineData(thuSet, chiSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()

        // Tổng kết
        val tongThu = thuEntries.sumOf { it.y.toDouble() }
        val tongChi = chiEntries.sumOf { it.y.toDouble() }
        val loiNhuan = tongThu - tongChi

        binding.tvTongThu.text = "Tổng thu: %,d VND".format(tongThu.toInt())
        binding.tvTongChi.text = "Tổng chi: %,d VND".format(tongChi.toInt())
        binding.tvLoiNhuan.text = "Lợi nhuận: %,d VND".format(loiNhuan.toInt())
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        return Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
