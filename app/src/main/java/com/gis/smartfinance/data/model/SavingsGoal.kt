package com.gis.smartfinance.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Date? = null,
    val icon: String,
    val color: String,
    val createdAt: Date = Date()
)