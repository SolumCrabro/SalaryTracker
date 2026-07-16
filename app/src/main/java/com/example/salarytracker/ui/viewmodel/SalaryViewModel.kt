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
import com.example.salarytracker.data.SalaryConfig
import com.example.salarytracker.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val dbKey: String // Ключ для связи с БД
)

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao = AppDatabase.getDatabase(application).transactionDao()

    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Получаем поток кастомных зарплат
    private val allSalaryConfigs = transactionDao.getAllSalaryConfigs()

    // Объединяем транзакции и зарплаты в один расчетный поток
    val monthlySummaries: StateFlow<List<MonthSummary>> = combine(
        allTransactions,
        allSalaryConfigs
    ) { transactions, configs ->
        val now = LocalDate.now()
        val monthsToDisplay = (0..3).map { now.minusMonths(it.toLong()) }

        // Превращаем список конфигов в удобную Map [КЛЮЧ -> ЗАРПЛАТА]
        val configsMap = configs.associate { it.monthYearKey to it.customSalary }

        monthsToDisplay.map { targetDate ->
            val monthNameEng = targetDate.month.name // Для стабильного ключа в БД
            val dbKey = "${monthNameEng}_${targetDate.year}"

            // Если в БД есть кастомная зарплата — берем ее, иначе дефолтные 75000
            val planSalary = configsMap[dbKey] ?: 75000.0

            val filteredTrans = transactions.filter {
                it.date.month == targetDate.month && it.date.year == targetDate.year
            }
            val totalPaid = filteredTrans.sumOf { it.amount }
            val debt = if (planSalary - totalPaid > 0) planSalary - totalPaid else 0.0

            MonthSummary(
                monthName = targetDate.month.getDisplayName(TextStyle.FULL, Locale("ru")).replaceFirstChar { it.uppercase() },
                year = targetDate.year,
                totalSalary = planSalary,
                totalPaid = totalPaid,
                debt = debt,
                isCurrentMonth = targetDate.month == now.month && targetDate.year == now.year,
                dbKey = dbKey
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(amount: Double, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTransaction = Transaction(amount = amount, date = date)
            transactionDao.insertTransaction(newTransaction)
        }
    }

    // НОВЫЙ МЕТОД: Обновление зарплаты для конкретного месяца
    fun updateSalaryForMonth(dbKey: String, newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertSalaryConfig(SalaryConfig(dbKey, newSalary))
        }
    }
}