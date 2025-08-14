package com.gis.smartfinance.data.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: String): List<String> =
        value.split(",").filter { it.isNotBlank() }

    @TypeConverter
    fun fromListString(list: List<String>): String = list.joinToString(",")
}