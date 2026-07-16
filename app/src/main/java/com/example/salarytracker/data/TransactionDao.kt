package com.example.salarytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM salary_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // --- НОВЫЕ ФУНКЦИИ ДЛЯ ЗАРПЛАТЫ ---

    // Сохраняет или обновляет зарплату для месяца (если ключ уже есть, перезапишет его)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSalaryConfig(config: SalaryConfig): Unit

    // Получает все измененные зарплаты
    @Query("SELECT * FROM salary_configs")
    fun getAllSalaryConfigs(): Flow<List<SalaryConfig>>
}