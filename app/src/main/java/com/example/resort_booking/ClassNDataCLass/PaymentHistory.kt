package com.example.resort_booking.ClassNDataCLass

import org.threeten.bp.LocalDateTime

data class PaymentHistory(
    val roomId: Int,
    val bookingTime: LocalDateTime,
    val checkInTime: LocalDateTime,
    val checkOutTime: LocalDateTime,
    val totalAmount: Double,
    val status: PaymentStatus,
    val avatarUrl: String
)

enum class PaymentStatus {
    PENDING,
    CONFIRMED,
    CANCELED
}
