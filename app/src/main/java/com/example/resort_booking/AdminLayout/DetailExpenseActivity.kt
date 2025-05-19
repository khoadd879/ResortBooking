package com.example.resort_booking.AdminLayout
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.ClassNDataCLass.Earn
import com.example.resort_booking.ClassNDataCLass.Expense
import com.example.resort_booking.ClassNDataCLass.TransactionAdapter
import com.example.resort_booking.ClassNDataCLass.TransactionItem
import com.example.resort_booking.R

class DetailExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = RecyclerView(this)
        setContentView(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fake data để test
        val transactions = listOf(
            TransactionItem.ExpenseItem(
                Expense("RS001", "Chi quảng cáo", 1000000.0, "2025-05-19")
            ),
            TransactionItem.EarnItem(
                Earn("RM001", "USR123", 1500000.0, "2025-05-18")
            ),
            TransactionItem.ExpenseItem(
                Expense("RS002", "Sửa chữa", 500000.0, "2025-05-17")
            )
        )

        adapter = TransactionAdapter(transactions)
        recyclerView.adapter = adapter
    }
}