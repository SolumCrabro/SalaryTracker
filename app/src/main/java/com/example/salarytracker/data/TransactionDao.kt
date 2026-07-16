package com.example.salarytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Позволяет вставить новую транзакцию в базу данных
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Unit

    // Получает ВСЕ внесенные деньги, сортируя их от самых новых к старым.
    // Используем Flow, чтобы интерфейс автоматически обновлялся при добавлении новых записей!
    @Query("SELECT * FROM salary_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
}