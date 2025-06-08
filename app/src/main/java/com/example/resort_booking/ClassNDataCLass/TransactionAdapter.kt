package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R
import data.Earn
import data.Expense
import data.TransactionItem

class TransactionAdapter(
    private val items: List<TransactionItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_EARN = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionItem.ExpenseItem -> TYPE_EXPENSE
            is TransactionItem.EarnItem -> TYPE_EARN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EXPENSE -> ExpenseViewHolder(inflater.inflate(R.layout.item_expense, parent, false))
            TYPE_EARN -> EarnViewHolder(inflater.inflate(R.layout.item_earn, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TransactionItem.ExpenseItem -> (holder as ExpenseViewHolder).bind(item.expense)
            is TransactionItem.EarnItem -> (holder as EarnViewHolder).bind(item.earn)
        }
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(expense: Expense) {
            itemView.findViewById<TextView>(R.id.tvCategory).text = expense.category
            itemView.findViewById<TextView>(R.id.tvAmount).text = "- ${expense.amount}"
            itemView.findViewById<TextView>(R.id.tvDate).text = expense.createDate
        }
    }

    class EarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(earn: Earn) {
            itemView.findViewById<TextView>(R.id.tvNameRoom).text = earn.roomResponse?.name_room ?: ""
            itemView.findViewById<TextView>(R.id.tvAmount).text = "+ ${earn.totalAmount}"
        }
    }
}

