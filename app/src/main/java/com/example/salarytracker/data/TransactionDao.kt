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
}