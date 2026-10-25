package com.example.salarytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaction::class, SalaryConfig::class, PendingProject::class],
    version = 4,
    exportSchema = true // Схемы сохраняются в app/schemas — по ним пишутся и проверяются миграции
)
@TypeConverters(DateConverter::class) // Подключаем наш конвертер дат
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /*
        Миграции схемы. При ЛЮБОМ изменении сущностей:
        1) увеличить version в @Database;
        2) добавить сюда Migration(старая, новая) с SQL-изменениями (ALTER TABLE / CREATE TABLE ...).
        Без миграции приложение упадет при обновлении, а не сотрет данные пользователя молча.
        */
        // 3 -> 4: частичные оплаты проектов (платеж ссылается на проект)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE salary_transactions ADD COLUMN projectId INTEGER")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_3_4)

        // Метод для создания/получения базы данных (Pattern Singleton)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salary_tracker_database"
                )
                    .addMigrations(*MIGRATIONS)
                    // Ранние тестовые сборки (схемы 1 и 2) по-прежнему пересоздаются, как и раньше
                    .fallbackToDestructiveMigrationFrom(1, 2)
                    // Установка более старой версии поверх новой — как и раньше, без падения
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
