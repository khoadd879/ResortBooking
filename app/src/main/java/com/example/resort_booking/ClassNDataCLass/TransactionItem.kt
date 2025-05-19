package com.example.resort_booking.ClassNDataCLass

sealed class TransactionItem {
    data class ExpenseItem(val expense: Expense) : TransactionItem()
    data class EarnItem(val earn: Earn) : TransactionItem()
}
