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

    // Поток кастомных зарплат из базы данных
    val allSalaryConfigs: Flow<List<SalaryConfig>> = transactionDao.getAllSalaryConfigs()

    // Главный расчетный поток
    val monthlySummaries: StateFlow<List<MonthSummary>> = combine(
        allTransactions,
        allSalaryConfigs,
        defaultSalary,
        startMonth,
        startYear,
        historyDepth
    ) { args ->
        // Извлекаем данные из массива строго по индексам их перечисления выше
        @Suppress("UNCHECKED_CAST")
        val transactions = args[0] as List<Transaction>

        @Suppress("UNCHECKED_CAST")
        val configs = args[1] as List<SalaryConfig>

        val defSalary = args[2] as Double
        val stMonth = args[3] as Int
        val stYear = args[4] as Int
        val depth = args[5] as Int

        val now = LocalDate.now()
        val configsMap = configs.associate { it.monthYearKey to it.customSalary }
        val startDate = LocalDate.of(stYear, stMonth, 1)

        // 1. Берем СУММУ ВООБЩЕ ВСЕХ внесенных денег из базы данных
        var totalMoneyAvailable = transactions.sumOf { it.amount }

        // Генерируем список месяцев в ХРОНОЛОГИЧЕСКОМ порядке (от старых к новым: Май -> Июнь -> Июль)
        val chronologicalMonths = (0 until depth).map { now.minusMonths(it.toLong()) }.reversed()
        val calculatedSummaries = mutableListOf<MonthSummary>()

        chronologicalMonths.forEach { targetDate ->
            val monthNameEng = targetDate.month.name
            val dbKey = "${monthNameEng}_${targetDate.year}"

            val isActive = !targetDate.withDayOfMonth(1).isBefore(startDate)
            val planSalary = configsMap[dbKey] ?: defSalary

            val debt: Double
            val totalPaidForThisMonth: Double

            if (isActive) {
                // 2. Распределяем общую сумму денег по цепочке месяцев, начиная с самого старого
                if (totalMoneyAvailable >= planSalary) {
                    // Если денег хватает на весь месяц целиком
                    totalPaidForThisMonth = planSalary
                    debt = 0.0
                    totalMoneyAvailable -= planSalary // Списываем потраченную часть денег
                } else if (totalMoneyAvailable > 0) {
                    // Если деньги остались, но их хватает только частично
                    totalPaidForThisMonth = totalMoneyAvailable
                    debt = planSalary - totalMoneyAvailable
                    totalMoneyAvailable = 0.0 // Все доступные деньги закончились
                } else {
                    // Денег на этот месяц уже не осталось
                    totalPaidForThisMonth = 0.0
                    debt = planSalary
                }
            } else {
                // Если по настройкам пользователя учет в этом месяце еще не велся
                totalPaidForThisMonth = 0.0
                debt = 0.0
            }

            calculatedSummaries.add(
                MonthSummary(
                    monthName = targetDate.month.getDisplayName(TextStyle.FULL, Locale("ru")).replaceFirstChar { it.uppercase() },
                    year = targetDate.year,
                    totalSalary = planSalary,
                    totalPaid = totalPaidForThisMonth,
                    debt = debt,
                    isCurrentMonth = targetDate.month == now.month && targetDate.year == now.year,
                    isActive = isActive,
                    dbKey = dbKey
                )
            )
        }

        // Разворачиваем список обратно, чтобы текущий месяц (Июль) отображался на самом верху таблицы
        calculatedSummaries.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Метод завершения первичной настройки приложения
    fun completeOnboarding(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.saveInitialSettings(salary, startMonth, startYear, depth)
        }
    }

    // Метод добавления платежа в базу данных
    fun addTransaction(amount: Double, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertTransaction(Transaction(amount = amount, date = date))
        }
    }

    // Метод изменения плановой зарплаты для конкретного месяца
    fun updateSalaryForMonth(dbKey: String, newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertSalaryConfig(SalaryConfig(dbKey, newSalary))
        }
    }
}