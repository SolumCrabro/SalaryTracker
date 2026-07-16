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
        val KEY_START_MONTH_OFFSET = intPreferencesKey("start_month_offset") // Сколько месяцев назад от текущего показывать историю
    }

    // По умолчанию считаем, что это первый запуск (true)
    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_IS_FIRST_RUN] ?: true
    }

    val defaultSalary: Flow<Double> = context.dataStore.data.map { pref ->
        pref[KEY_DEFAULT_SALARY] ?: 75000.0
    }

    val startMonthOffset: Flow<Int> = context.dataStore.data.map { pref ->
        pref[KEY_START_MONTH_OFFSET] ?: 0
    }

    suspend fun saveInitialSettings(salary: Double, monthOffset: Int) {
        context.dataStore.edit { pref ->
            pref[KEY_DEFAULT_SALARY] = salary
            pref[KEY_START_MONTH_OFFSET] = monthOffset
            pref[KEY_IS_FIRST_RUN] = false // Записываем напрямую без лишних проверок!
        }
    }
}