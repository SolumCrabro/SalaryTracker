package com.example.salarytracker.domain

import com.example.salarytracker.data.PendingProject
import com.example.salarytracker.data.Transaction

// Проект в работе вместе с его оплатами (частичными платежами)
data class ProjectProgress(
    val project: PendingProject,
    val payments: List<Transaction>
) {
    val paid: Double = payments.sumOf { it.amount }
    val remaining: Double = (project.amount - paid).coerceAtLeast(0.0)
    val fraction: Float =
        if (project.amount > 0) (paid / project.amount).coerceIn(0.0, 1.0).toFloat() else 0f
    val hasPayments: Boolean get() = payments.isNotEmpty()

    // Закрывает ли платеж amount проект полностью (с допуском на копейки)
    fun isClosedBy(amount: Double): Boolean = isClosed(project.amount, paid + amount)

    companion object {
        private const val EPSILON = 0.005

        fun isClosed(projectAmount: Double, totalPaid: Double): Boolean = totalPaid >= projectAmount - EPSILON

        fun build(projects: List<PendingProject>, transactions: List<Transaction>): List<ProjectProgress> {
            val byProject = transactions.filter { it.projectId != null }.groupBy { it.projectId }
            return projects.map { project ->
                ProjectProgress(
                    project = project,
                    payments = byProject[project.id].orEmpty().sortedWith(compareBy({ it.date }, { it.id }))
                )
            }
        }
    }
}
