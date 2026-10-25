package com.example.salarytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM salary_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // --- ФУНКЦИЯ ДЛЯ УДАЛЕНИЯ ПЛАТЕЖА ---
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // --- ФУНКЦИИ ДЛЯ ЗАРПЛАТЫ ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryConfig(config: SalaryConfig)

    @Query("SELECT * FROM salary_configs")
    fun getAllSalaryConfigs(): Flow<List<SalaryConfig>>

    // --- ФУНКЦИИ ДЛЯ ПРОЕКТОВ В РАБОТЕ ---
    @Insert
    suspend fun insertProject(project: PendingProject)

    @Query("SELECT * FROM pending_projects ORDER BY date DESC")
    fun getAllProjects(): Flow<List<PendingProject>>

    @Delete
    suspend fun deleteProject(project: PendingProject)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM salary_transactions WHERE projectId = :projectId")
    suspend fun getProjectPaidSum(projectId: Int): Double

    // --- ФУНКЦИИ ДЛЯ РЕЗЕРВНОЙ КОПИИ ---
    @Query("SELECT * FROM salary_transactions ORDER BY date ASC, id ASC")
    suspend fun getAllTransactionsOnce(): List<Transaction>

    @Query("SELECT * FROM salary_configs")
    suspend fun getAllSalaryConfigsOnce(): List<SalaryConfig>

    @Query("SELECT * FROM pending_projects ORDER BY date ASC, id ASC")
    suspend fun getAllProjectsOnce(): List<PendingProject>

    @Query("DELETE FROM salary_transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM salary_configs")
    suspend fun deleteAllSalaryConfigs()

    @Query("DELETE FROM pending_projects")
    suspend fun deleteAllProjects()

    @Insert
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryConfigs(configs: List<SalaryConfig>)

    @Insert
    suspend fun insertProjects(projects: List<PendingProject>)
}
