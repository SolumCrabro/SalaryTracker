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
import com.example.salarytracker.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Класс-модель для отображения строки месяца в таблице
data class MonthSummary(
    val monthName: String,
    val year: Int,
    val totalSalary: Double = 1500.0, // Плановая зарплата (можешь поменять цифру)
    val totalPaid: Double,             // Сколько уже внесено денег
    val debt: Double,                  // Остаток (Плановые - Внесенные)
    val isCurrentMonth: Boolean
)

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    // Инициализируем DAO через наш синглтон базы данных
    private val transactionDao = AppDatabase.getDatabase(application).transactionDao()

    // Превращаем Flow из Room в StateFlow, который Compose умеет читать и автоматически обновлять UI
    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Самая важная часть: превращаем список транзакций в готовую таблицу месяцев!
    val monthlySummaries: StateFlow<List<MonthSummary>> = allTransactions.map { transactions ->
        val now = LocalDate.now()

        // Создаем список для 4 месяцев: текущий и 3 предыдущих (как на эскизе)
        val monthsToDisplay = (0..3).map { now.minusMonths(it.toLong()) }

        monthsToDisplay.map { targetDate ->
            // Фильтруем транзакции, которые относятся к этому месяцу и году
            val filteredTrans = transactions.filter {
                it.date.month == targetDate.month && it.date.year == targetDate.year
            }

            val totalPaid = filteredTrans.sumOf { it.amount }
            val planSalary = 75000.0 // Фиксированная зарплата для расчета
            val debt = if (planSalary - totalPaid > 0) planSalary - totalPaid else 0.0

            MonthSummary(
                monthName = targetDate.month.getDisplayName(TextStyle.FULL, Locale("ru")).replaceFirstChar { it.uppercase() },
                year = targetDate.year,
                totalSalary = planSalary,
                totalPaid = totalPaid,
                debt = debt,
                isCurrentMonth = targetDate.month == now.month && targetDate.year == now.year
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Функция сохранения новой записи. Запускается в фоновом потоке (Coroutine)
    fun addTransaction(amount: Double, date: LocalDate) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {   // Переключаем на фоновый поток IO
            val newTransaction = Transaction(
                amount = amount,
                date = date
            )
            transactionDao.insertTransaction(newTransaction)
        }
    }
}