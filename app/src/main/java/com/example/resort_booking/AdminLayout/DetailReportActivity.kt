package com.example.resort_booking.AdminLayout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ApiClient
import com.example.resort_booking.DetailReportAdapter
import com.example.resort_booking.R
import data.*
import interfaceAPI.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DetailReportActivity : AppCompatActivity(), DetailReportAdapter.Listener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DetailReportAdapter
    private lateinit var btnAdd: Button
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner

    private var resortId: String? = null
    private lateinit var apiService: ApiService
    private var isFirstLoadDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_expense_acitivity)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAdd = findViewById(R.id.btnAddExpense)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)

        resortId = intent.getStringExtra("RESORT_ID")

        val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        apiService = ApiClient.create(sharedPreferences)

        val months = (1..12).map { it.toString().padStart(2, '0') }
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..(currentYear + 2)).map { it.toString() }
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)

        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
        spinnerYear.setSelection(years.indexOf(currentYear.toString()))

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (isFirstLoadDone) {
                    loadReport()
                } else {
                    isFirstLoadDone = spinnerMonth.selectedItem != null && spinnerYear.selectedItem != null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerMonth.onItemSelectedListener = spinnerListener
        spinnerYear.onItemSelectedListener = spinnerListener

        btnAdd.setOnClickListener {
            val intent = Intent(this, CreateExpense::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        loadReport()
    }

    private fun loadReport() {
        val month = spinnerMonth.selectedItem.toString().toInt()
        val year = spinnerYear.selectedItem.toString().toInt()

        val request = ReportRequest(
            idResort = resortId ?: return,
            reportMonth = month,
            reportYear = year
        )

        apiService.getReport(request).enqueue(object : Callback<ApiResponseWrapper<ReportResponse>> {
            override fun onResponse(
                call: Call<ApiResponseWrapper<ReportResponse>>,
                response: Response<ApiResponseWrapper<ReportResponse>>
            ) {
                if (response.isSuccessful) {
                    val report = response.body()?.data

                    if (report == null || report.details.isEmpty()) {
                        // Không có dữ liệu
                        recyclerView.adapter = null
                        Toast.makeText(this@DetailReportActivity, "Không có dữ liệu cho tháng/năm này", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter = DetailReportAdapter(report.details, this@DetailReportActivity)
                        recyclerView.adapter = adapter
                    }

                } else if (response.code() == 404) {
                    // Trường hợp 404 do không có dữ liệu
                    recyclerView.adapter = null
                    Toast.makeText(this@DetailReportActivity, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DetailReportActivity, "Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponseWrapper<ReportResponse>>, t: Throwable) {
                Toast.makeText(this@DetailReportActivity, "Lỗi mạng: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDelete(detail: ReportDetail) {
        Toast.makeText(this, "Xóa mục: ${detail.category ?: detail.titleOfExpense ?: detail.titleOfIncome}", Toast.LENGTH_SHORT).show()
    }

}
