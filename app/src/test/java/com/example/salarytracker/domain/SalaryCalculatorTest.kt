package com.example.salarytracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SalaryCalculatorTest {

    private fun calc(
        paid: Double,
        start: LocalDate,
        today: LocalDate,
        salary: Double = 1000.0,
        overrides: Map<String, Double> = emptyMap()
    ) = SalaryCalculator.calculate(paid, overrides, salary, start.monthValue, start.year, today)

    @Test
    fun monthsGoFromStartToCurrentAcrossYearBoundary() {
        val result = calc(0.0, LocalDate.of(2025, 11, 1), LocalDate.of(2026, 2, 15))
        assertEquals(
            listOf("NOVEMBER_2025", "DECEMBER_2025", "JANUARY_2026", "FEBRUARY_2026"),
            result.map { it.dbKey }
        )
        assertEquals(listOf(2025, 2025, 2026, 2026), result.map { it.year })
        assertTrue(result.last().isCurrentMonth)
        assertTrue(result.all { it.isActive })
    }

    @Test
    fun paymentsCoverOldestMonthsFirstAcrossYears() {
        val result = calc(2500.0, LocalDate.of(2025, 11, 1), LocalDate.of(2026, 2, 15))
        assertEquals(listOf(0.0, 0.0, 500.0, 1000.0), result.map { it.debt })
        assertEquals(1500.0, SalaryCalculator.totalBalance(result), 0.001)
    }

    @Test
    fun totalBalanceCountsAllMonthsNotOnlyVisibleOnes() {
        // 12 месяцев без выплат: долг за весь год, даже если на главной видно только 3
        val result = calc(0.0, LocalDate.of(2025, 3, 1), LocalDate.of(2026, 2, 1))
        assertEquals(12, result.size)
        assertEquals(12000.0, SalaryCalculator.totalBalance(result), 0.001)
        assertEquals(3000.0, result.takeLast(3).sumOf { it.debt }, 0.001)
    }

    @Test
    fun overpaymentGoesToCurrentMonthAsNegativeBalance() {
        val result = calc(3500.0, LocalDate.of(2025, 12, 1), LocalDate.of(2026, 2, 10))
        assertEquals(500.0, result.last().surplus, 0.001)
        assertEquals(-500.0, SalaryCalculator.totalBalance(result), 0.001)
        assertTrue(result.all { it.debt == 0.0 })
    }

    @Test
    fun salaryOverrideIsUsedForItsMonthOnly() {
        val result = calc(
            paid = 0.0,
            start = LocalDate.of(2025, 12, 1),
            today = LocalDate.of(2026, 1, 20),
            overrides = mapOf("DECEMBER_2025" to 1500.0)
        )
        assertEquals(listOf(1500.0, 1000.0), result.map { it.totalSalary })
        assertEquals(2500.0, SalaryCalculator.totalBalance(result), 0.001)
    }

    @Test
    fun futureStartMonthShowsInactiveCurrentMonthWithoutDebt() {
        val result = calc(0.0, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 3, 1))
        assertEquals(1, result.size)
        assertFalse(result.first().isActive)
        assertEquals(0.0, SalaryCalculator.totalBalance(result), 0.001)
    }
}
