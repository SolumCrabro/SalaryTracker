package com.example.salarytracker.data

/*
Этот класс будет отвечать за асинхронное сохранение и
чтение базовой зарплаты и стартового месяца*/

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

class AppSettings(private val context: Context) {
    companion object {
        val KEY_IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val KEY_DEFAULT_SALARY = doublePreferencesKey("default_salary")
        val KEY_START_MONTH = intPreferencesKey("start_month") // Номер месяца старта (1-12)
        val KEY_START_YEAR = intPreferencesKey("start_year")   // Год старта (например, 2026)
        val KEY_HISTORY_DEPTH = intPreferencesKey("history_depth") // Сколько месяцев показывать (1, 2, 3)
    }

    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_FIRST_RUN] ?: true }
    val defaultSalary: Flow<Double> = context.dataStore.data.map { it[KEY_DEFAULT_SALARY] ?: 75000.0 }
    val startMonth: Flow<Int> = context.dataStore.data.map { it[KEY_START_MONTH] ?: 1 }
    val startYear: Flow<Int> = context.dataStore.data.map { it[KEY_START_YEAR] ?: 2026 }
    val historyDepth: Flow<Int> = context.dataStore.data.map { it[KEY_HISTORY_DEPTH] ?: 3 }

    suspend fun saveInitialSettings(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        context.dataStore.edit { pref ->
            pref[KEY_DEFAULT_SALARY] = salary
            pref[KEY_START_MONTH] = startMonth
            pref[KEY_START_YEAR] = startYear
            pref[KEY_HISTORY_DEPTH] = depth
            pref[KEY_IS_FIRST_RUN] = false
        }
    }
}