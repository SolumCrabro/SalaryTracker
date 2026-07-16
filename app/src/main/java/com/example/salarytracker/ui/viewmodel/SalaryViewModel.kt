package com.example.salarytracker.ui.viewmodel

/*
Этот класс будет принимать нашу базу данных (TransactionDao),
предоставлять список всех транзакций и содержать функцию для
сохранения новой записи.
*/

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salarytracker.data.AppDatabase
import com.example.salarytracker.data.AppSettings
import com.example.salarytracker.data.SalaryConfig
import com.example.salarytracker.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class MonthSummary(
    val monthName: String,
    val year: Int,
    val totalSalary: Double,
    val totalPaid: Double,
    val debt: Double,
    val isCurrentMonth: Boolean,
    val isActive: Boolean,
    val dbKey: String
)

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao = AppDatabase.getDatabase(application).transactionDao()
    private val appSettings = AppSettings(application)

    val isFirstRun = appSettings.isFirstRun.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val defaultSalary = appSettings.defaultSalary
    private val startMonth = appSettings.startMonth
    private val startYear = appSettings.startYear
    private val historyDepth = appSettings.historyDepth

    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allSalaryConfigs = transactionDao.getAllSalaryConfigs()

    // ИСКУССТВЕННО ОБЪЕДИНЯЕМ 6 ПОТОКОВ ЧЕРЕЗ МАССИВ, ЧТОБЫ КОМПИЛЯТОР ЗНАЛ ТИПЫ ДАННЫХ
    val monthlySummaries: StateFlow<List<MonthSummary>> = combine(
        allTransactions,
        allSalaryConfigs,
        defaultSalary,
        startMonth,
        startYear,
        historyDepth
    ) { args ->
        // Извлекаем элементы строго по индексам в порядке их перечисления в combine!
        @Suppress("UNCHECKED_CAST")
        val transactions = args[0] as List<Transaction>
        @Suppress("UNCHECKED_CAST")
        val configs = args[1] as List<SalaryConfig>
        val defSalary = args[2] as Double
        val stMonth = args[3] as Int
        val stYear = args[4] as Int
        val depth = args[5] as Int

        val now = LocalDate.now()
        val monthsToDisplay = (0 until depth).map { now.minusMonths(it.toLong()) }
        val configsMap = configs.associate { it.monthYearKey to it.customSalary }
        val startDate = LocalDate.of(stYear, stMonth, 1)

        monthsToDisplay.map { targetDate ->
            val monthNameEng = targetDate.month.name
            val dbKey = "${monthNameEng}_${targetDate.year}"

            val isActive = !targetDate.withDayOfMonth(1).isBefore(startDate)
            val planSalary = configsMap[dbKey] ?: defSalary

            val filteredTrans = transactions.filter {
                it.date.month == targetDate.month && it.date.year == targetDate.year
            }
            val totalPaid = filteredTrans.sumOf { it.amount }
            val debt = if (isActive && (planSalary - totalPaid > 0)) planSalary - totalPaid else 0.0

            MonthSummary(
                monthName = targetDate.month.getDisplayName(TextStyle.FULL, Locale("ru")).replaceFirstChar { it.uppercase() },
                year = targetDate.year,
                totalSalary = planSalary,
                totalPaid = totalPaid,
                debt = debt,
                isCurrentMonth = targetDate.month == now.month && targetDate.year == now.year,
                isActive = isActive,
                dbKey = dbKey
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completeOnboarding(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.saveInitialSettings(salary, startMonth, startYear, depth)
        }
    }

    fun addTransaction(amount: Double, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertTransaction(Transaction(amount = amount, date = date))
        }
    }

    fun updateSalaryForMonth(dbKey: String, newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertSalaryConfig(SalaryConfig(dbKey, newSalary))
        }
    }
}