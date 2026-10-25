package com.example.salarytracker.domain

import com.example.salarytracker.data.PendingProject
import com.example.salarytracker.data.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProjectProgressTest {

    private val project = PendingProject(id = 1, title = "Шкаф", amount = 750.0, date = LocalDate.of(2026, 8, 12))

    private fun payment(id: Int, amount: Double, day: Int, projectId: Int? = 1) = Transaction(
        id = id, amount = amount, date = LocalDate.of(2026, 8, day), paymentType = "CARD",
        isSideIncome = true, sourceNote = "Шкаф", projectId = projectId
    )

    @Test
    fun partialPaymentsGiveFractionAndRemaining() {
        val progress = ProjectProgress.build(
            listOf(project),
            listOf(payment(2, 150.0, 18), payment(1, 150.0, 14), payment(3, 500.0, 15, projectId = null))
        ).single()
        assertEquals(listOf(1, 2), progress.payments.map { it.id })
        assertEquals(300.0, progress.paid, 0.001)
        assertEquals(450.0, progress.remaining, 0.001)
        assertEquals(0.4f, progress.fraction, 0.001f)
        assertTrue(progress.hasPayments)
    }

    @Test
    fun projectWithoutPaymentsIsUntouched() {
        val progress = ProjectProgress.build(listOf(project), listOf(payment(1, 100.0, 1, projectId = 2))).single()
        assertFalse(progress.hasPayments)
        assertEquals(750.0, progress.remaining, 0.001)
        assertEquals(0f, progress.fraction, 0.001f)
    }

    @Test
    fun closingRuleUsesRemaining() {
        val progress = ProjectProgress(project, listOf(payment(1, 300.0, 14)))
        assertFalse(progress.isClosedBy(449.0))
        assertTrue(progress.isClosedBy(450.0))
        assertTrue(progress.isClosedBy(1000.0))
    }

    @Test
    fun overpaymentClampsProgress() {
        val progress = ProjectProgress(project, listOf(payment(1, 900.0, 14)))
        assertEquals(0.0, progress.remaining, 0.001)
        assertEquals(1f, progress.fraction, 0.001f)
    }
}
