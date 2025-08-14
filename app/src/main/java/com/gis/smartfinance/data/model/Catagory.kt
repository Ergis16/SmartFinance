package com.gis.smartfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val monthlyBudget: Double? = null
)