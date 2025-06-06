package com.example.resort_booking.ClassNDataCLass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resort_booking.R
import kotlin.text.category

class TransactionAdapter(
   // private val items: List<TransactionItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_EARN = 1
    }

    override fun getItemViewType(position: Int): Int {
//        return when (items[position]) {
//            is TransactionItem.ExpenseItem -> TYPE_EXPENSE
//            is TransactionItem.EarnItem -> TYPE_EARN
//        }
        return TODO("Provide the return value")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EXPENSE -> {
                val view = inflater.inflate(R.layout.item_expense, parent, false)
                ExpenseViewHolder(view)
            }
            TYPE_EARN -> {
                val view = inflater.inflate(R.layout.item_earn, parent, false)
                EarnViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (val item = items[position]) {
//            is TransactionItem.ExpenseItem -> (holder as ExpenseViewHolder).bind(item.expense)
//            is TransactionItem.EarnItem -> (holder as EarnViewHolder).bind(item.earn)
//        }
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(expense: Expense) {
            itemView.findViewById<TextView>(R.id.tvExpenseId).text = "ID_RS: ${expense.idRs}"
            itemView.findViewById<TextView>(R.id.tvCategory).text = "Category: ${expense.category}"
            itemView.findViewById<TextView>(R.id.tvAmount).text = "Amount: ${expense.amount}"
            itemView.findViewById<TextView>(R.id.tvDate).text = "Date: ${expense.createDate}"
        }
    }

    class EarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(earn: Earn) {
            itemView.findViewById<TextView>(R.id.tvRoomId).text = "ID_ROOM: ${earn.idRoom}"
            itemView.findViewById<TextView>(R.id.tvUserId).text = "ID_USER: ${earn.idUser}"
            itemView.findViewById<TextView>(R.id.tvTotal).text = "Total: ${earn.totalAmount}"
            itemView.findViewById<TextView>(R.id.tvDateEarn).text = "Date: ${earn.createDate}"
        }
    }
}
