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
    val paymentType: String, // "CARD" или "CASH"
    val isSideIncome: Boolean = false, // НОВОЕ ПОЛЕ: true = левый приход / подработка
    val sourceNote: String? = null,    // НОВОЕ ПОЛЕ: Название проекта/подработки
    val projectId: Int? = null,        // Оплата (в т.ч. частичная) проекта из pending_projects
    // Внесено через конвертер: исходная сумма и валюта, курс (BYN за 1 USD) и банк
    val originalAmount: Double? = null,
    val originalCurrency: String? = null,
    val exchangeRate: Double? = null,
    val rateSource: String? = null
)

class DateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(formatter)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it, formatter) }
}