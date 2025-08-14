package com.gis.smartfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "transactions")
data class FinancialTransaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val description: String = "",
    val date: Date = Date(),
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null,  // Changed from enum to String
    val tags: String = "",  // Changed from List<String> to String
    val location: String? = null,
    val merchantName: String? = null,
    val receiptImagePath: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class RecurringPeriod {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
}
