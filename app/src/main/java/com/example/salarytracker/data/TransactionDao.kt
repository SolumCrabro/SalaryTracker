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
    fun insertTransaction(transaction: Transaction): Unit

    @Query("SELECT * FROM salary_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // --- ФУНКЦИЯ ДЛЯ УДАЛЕНИЯ ПЛАТЕЖА ---
    @Delete
    fun deleteTransaction(transaction: Transaction): Unit

    // --- ФУНКЦИИ ДЛЯ ЗАРПЛАТЫ ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSalaryConfig(config: SalaryConfig): Unit

    @Query("SELECT * FROM salary_configs")
    fun getAllSalaryConfigs(): Flow<List<SalaryConfig>>
}