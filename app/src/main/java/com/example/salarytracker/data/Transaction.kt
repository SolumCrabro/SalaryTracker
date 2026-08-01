package com.example.salarytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "salary_transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val date: LocalDate,
    val paymentType: String // НОВОЕ ПОЛЕ: "CARD" или "CASH"
)

class DateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(formatter)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it, formatter) }
}