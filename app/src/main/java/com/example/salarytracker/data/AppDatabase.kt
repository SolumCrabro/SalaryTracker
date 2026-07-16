package com.example.salarytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Transaction::class, SalaryConfig::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class) // Подключаем наш конвертер дат
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Метод для создания/получения базы данных (Pattern Singleton)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salary_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}