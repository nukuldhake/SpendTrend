package com.example.spend_trend.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }
}