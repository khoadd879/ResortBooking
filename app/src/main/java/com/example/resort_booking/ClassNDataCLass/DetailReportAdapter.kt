package com.example.resort_booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import data.ReportDetail

class DetailReportAdapter(
    private val items: List<ReportDetail>,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Listener {
        fun onDelete(detail: ReportDetail)
    }

    companion object {
        const val TYPE_THU = 1
        const val TYPE_CHI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == "Thu") TYPE_THU else TYPE_CHI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_THU) {
            val view = inflater.inflate(R.layout.item_earn, parent, false)
            IncomeViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_expense, parent, false)
            ExpenseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detail = items[position]
        if (holder is IncomeViewHolder) holder.bind(detail)
        else if (holder is ExpenseViewHolder) holder.bind(detail)
    }

    override fun getItemCount() = items.size

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoom = itemView.findViewById<TextView>(R.id.tvRoomId)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDateEarn)
        private val tvTotal = itemView.findViewById<TextView>(R.id.tvTotal)
        private val btnDeleteEarn = itemView.findViewById<ImageButton>(R.id.btnDeleteEarn)

        fun bind(detail: ReportDetail) {
            tvRoom.text = detail.titleOfIncome ?: "Không có phòng"
            tvDate.text = detail.createDate
            tvTotal.text = String.format("%,.2f", detail.amount)

            btnDeleteEarn.setOnClickListener { listener.onDelete(detail) }
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExpense = itemView.findViewById<TextView>(R.id.tvExpenseId)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)

        private val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)

        fun bind(detail: ReportDetail) {
            tvExpense.text = detail.titleOfExpense // Hiển thị titleOfExpense
            tvDate.text = detail.createDate
            tvAmount.text = String.format("%,.2f", detail.amount)

            btnDelete.setOnClickListener { listener.onDelete(detail) }
        }
    }
}
