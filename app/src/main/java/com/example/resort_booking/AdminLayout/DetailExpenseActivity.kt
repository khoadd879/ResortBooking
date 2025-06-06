package com.example.resort_booking.AdminLayout
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.Earn
import com.example.resort_booking.ClassNDataCLass.Expense
import com.example.resort_booking.ClassNDataCLass.TransactionAdapter
import com.example.resort_booking.R


class DetailExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_expense_acitivity)
        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val apiService = com.example.resort_booking.ApiClient.create(sharedPreferences)
        val resortId = intent.getStringExtra("RESORT_ID")
        val btnAdd = findViewById<Button>(R.id.btnAddExpense)
        val btnResort = findViewById<Button>(R.id.ResortList)

        btnAdd.setOnClickListener {
            val intent = Intent(this, CreateExpense::class.java)
            intent.putExtra("RESORT_ID", resortId)
            startActivity(intent)
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val resortId = result.data?.getStringExtra("RESORT_ID")
                Log.d("DetailExpenseActivity", "Resort ID: $resortId")
                if (resortId != null) {
                    // TODO: sử dụng resortId để hiển thị dữ liệu chi tiêu cho resort đó

                }
            }
        }

        btnResort.setOnClickListener {
            val intent = Intent(this, ResortListActivity::class.java)
            intent.putExtra("SELECT_MODE", true)
            launcher.launch(intent)
        }

    }
}