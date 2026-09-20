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
    val surplus: Double, // НОВОЕ ПОЛЕ: Сумма профицита (переплаты) в этом месяце
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

    val allSalaryConfigs: Flow<List<SalaryConfig>> = transactionDao.getAllSalaryConfigs()

    val monthlySummaries: StateFlow<List<MonthSummary>> = combine(
        allTransactions, allSalaryConfigs, defaultSalary, startMonth, startYear, historyDepth
    ) { args ->
        @Suppress("UNCHECKED_CAST") val transactions = args[0] as List<Transaction>
        @Suppress("UNCHECKED_CAST") val configs = args[1] as List<SalaryConfig>
        val defSalary = args[2] as Double
        val stMonth = args[3] as Int
        val stYear = args[4] as Int
        val depth = args[5] as Int

        val now = LocalDate.now()
        val configsMap = configs.associate { it.monthYearKey to it.customSalary }
        val startFirstDay = LocalDate.of(stYear, stMonth, 1)
        val currentFirstDay = LocalDate.of(now.year, now.monthValue, 1)

        // 1. Формируем полную хронологическую цепочку от стартового месяца до текущего (или до (now - depth), если старт давно)
        val firstCalculationMonth = if (startFirstDay.isBefore(currentFirstDay)) startFirstDay else currentFirstDay
        
        val fullMonthsChain = mutableListOf<LocalDate>()
        var curr = firstCalculationMonth
        while (!curr.isAfter(currentFirstDay)) {
            fullMonthsChain.add(curr)
            curr = curr.plusMonths(1)
        }

        // Считаем общую сумму ВСЕХ денег
        var totalMoneyAvailable = transactions.sumOf { it.amount }

        val allCalculatedSummaries = mutableListOf<MonthSummary>()

        fullMonthsChain.forEach { targetDate ->
            val monthNameEng = targetDate.month.name
            val dbKey = "${monthNameEng}_${targetDate.year}"

            val isActive = !targetDate.isBefore(startFirstDay)
            val planSalary = configsMap[dbKey] ?: defSalary

            val debt: Double
            val surplus: Double
            val totalPaidForThisMonth: Double

            if (isActive) {
                if (totalMoneyAvailable >= planSalary) {
                    // ЕСЛИ ДЕНЕГ ХВАТАЕТ НА ПЛАН ЗАРПЛАТЫ:
                    debt = 0.0

                    val isCurrent = targetDate.month == now.month && targetDate.year == now.year
                    if (isCurrent) {
                        surplus = totalMoneyAvailable - planSalary
                        totalPaidForThisMonth = planSalary + surplus
                    } else {
                        surplus = 0.0
                        totalPaidForThisMonth = planSalary
                    }

                    totalMoneyAvailable -= planSalary
                } else if (totalMoneyAvailable > 0) {
                    // Частичное погашение
                    totalPaidForThisMonth = totalMoneyAvailable
                    debt = planSalary - totalMoneyAvailable
                    surplus = 0.0
                    totalMoneyAvailable = 0.0
                } else {
                    // Денег нет
                    totalPaidForThisMonth = 0.0
                    debt = planSalary
                    surplus = 0.0
                }
            } else {
                totalPaidForThisMonth = 0.0
                debt = 0.0
                surplus = 0.0
            }

            allCalculatedSummaries.add(
                MonthSummary(
                    monthName = targetDate.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() },
                    year = targetDate.year,
                    totalSalary = planSalary,
                    totalPaid = totalPaidForThisMonth,
                    debt = debt,
                    surplus = surplus,
                    isCurrentMonth = targetDate.month == now.month && targetDate.year == now.year,
                    isActive = isActive,
                    dbKey = dbKey
                )
            )
        }

        // Фильтруем выводимый список согласно глубине истории (depth), разворачивая список свежими месяцами наверх
        allCalculatedSummaries.takeLast(depth).reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completeOnboarding(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.saveInitialSettings(salary, startMonth, startYear, depth)
        }
    }

    fun addTransaction(amount: Double, date: LocalDate, paymentType: String) { // Добавили параметр
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertTransaction(
                Transaction(amount = amount, date = date, paymentType = paymentType)
            )
        }
    }

    fun updateSalaryForMonth(dbKey: String, newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertSalaryConfig(SalaryConfig(dbKey, newSalary))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction)
        }
    }
}