package com.example.salarytracker.domain

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class MonthSummary(
    val monthName: String,
    val year: Int,
    val totalSalary: Double,
    val totalPaid: Double,
    val debt: Double,
    val surplus: Double, // Сумма профицита (переплаты) — бывает только у текущего месяца
    val isCurrentMonth: Boolean,
    val isActive: Boolean,
    val dbKey: String
)

/*
Чистый расчет долга без Android-зависимостей (покрыт юнит-тестами).
Все выплаты по окладу складываются в общий котел и гасят месяцы
по порядку, начиная с самого старого. Остаток уходит в переплату текущего месяца.
*/
object SalaryCalculator {

    private val ruLocale = Locale("ru")

    // Ключ месяца в БД, например "OCTOBER_2026"
    fun monthKey(date: LocalDate): String = "${date.month.name}_${date.year}"

    fun calculate(
        mainIncomeTotal: Double,
        salaryOverrides: Map<String, Double>,
        defaultSalary: Double,
        startMonth: Int,
        startYear: Int,
        today: LocalDate
    ): List<MonthSummary> {
        val startFirstDay = LocalDate.of(startYear, startMonth, 1)
        val currentFirstDay = today.withDayOfMonth(1)

        // Полная хронологическая цепочка от стартового месяца до текущего (через границы годов)
        val firstCalculationMonth = if (startFirstDay.isBefore(currentFirstDay)) startFirstDay else currentFirstDay
        val fullMonthsChain = mutableListOf<LocalDate>()
        var curr = firstCalculationMonth
        while (!curr.isAfter(currentFirstDay)) {
            fullMonthsChain.add(curr)
            curr = curr.plusMonths(1)
        }

        var totalMoneyAvailable = mainIncomeTotal

        return fullMonthsChain.map { targetDate ->
            val dbKey = monthKey(targetDate)
            val isActive = !targetDate.isBefore(startFirstDay)
            val isCurrent = targetDate == currentFirstDay
            val planSalary = salaryOverrides[dbKey] ?: defaultSalary

            val debt: Double
            val surplus: Double
            val totalPaidForThisMonth: Double

            if (isActive) {
                if (totalMoneyAvailable >= planSalary) {
                    debt = 0.0
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
                    totalPaidForThisMonth = 0.0
                    debt = planSalary
                    surplus = 0.0
                }
            } else {
                totalPaidForThisMonth = 0.0
                debt = 0.0
                surplus = 0.0
            }

            MonthSummary(
                monthName = targetDate.month.getDisplayName(TextStyle.FULL_STANDALONE, ruLocale).replaceFirstChar { it.uppercase() },
                year = targetDate.year,
                totalSalary = planSalary,
                totalPaid = totalPaidForThisMonth,
                debt = debt,
                surplus = surplus,
                isCurrentMonth = isCurrent,
                isActive = isActive,
                dbKey = dbKey
            )
        }
    }

    // Баланс для карточки на главной: отрицательное число = переплата, положительное = общий долг
    fun totalBalance(summaries: List<MonthSummary>): Double {
        val currentSurplus = summaries.find { it.isCurrentMonth }?.surplus ?: 0.0
        return if (currentSurplus > 0) -currentSurplus else summaries.filter { it.isActive }.sumOf { it.debt }
    }
}
