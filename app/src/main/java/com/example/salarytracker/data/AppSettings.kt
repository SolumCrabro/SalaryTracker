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

enum class ThemeMode(val title: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная");

    companion object {
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

class AppSettings(private val context: Context) {
    companion object {
        val KEY_IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val KEY_DEFAULT_SALARY = doublePreferencesKey("default_salary")
        val KEY_START_MONTH = intPreferencesKey("start_month") // Номер месяца старта (1-12)
        val KEY_START_YEAR = intPreferencesKey("start_year")   // Год старта (например, 2026)
        val KEY_HISTORY_DEPTH = intPreferencesKey("history_depth") // Сколько месяцев показывать (1, 2, 3)
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM / LIGHT / DARK
        val KEY_CURRENCY = stringPreferencesKey("currency")     // USD / BYN — валюта учёта
        val KEY_RATE_BANK = stringPreferencesKey("rate_bank")   // Банк для конвертера
        fun rateKey(bank: Bank) = stringPreferencesKey("rate_${bank.name}") // Последний полученный курс
    }

    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_FIRST_RUN] ?: true }
    val defaultSalary: Flow<Double> = context.dataStore.data.map { it[KEY_DEFAULT_SALARY] ?: 75000.0 }
    val startMonth: Flow<Int> = context.dataStore.data.map { it[KEY_START_MONTH] ?: 1 }
    val startYear: Flow<Int> = context.dataStore.data.map { it[KEY_START_YEAR] ?: 2026 }
    val historyDepth: Flow<Int> = context.dataStore.data.map { it[KEY_HISTORY_DEPTH] ?: 3 }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { ThemeMode.fromName(it[KEY_THEME_MODE]) }
    val currency: Flow<AppCurrency> = context.dataStore.data.map { AppCurrency.fromName(it[KEY_CURRENCY]) }
    val rateBank: Flow<Bank> = context.dataStore.data.map { Bank.fromName(it[KEY_RATE_BANK]) }
    val cachedRates: Flow<Map<Bank, ExchangeRate>> = context.dataStore.data.map { pref ->
        Bank.entries.mapNotNull { bank -> ExchangeRate.deserialize(bank, pref[rateKey(bank)])?.let { bank to it } }.toMap()
    }

    suspend fun saveInitialSettings(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        context.dataStore.edit { pref ->
            pref[KEY_DEFAULT_SALARY] = salary
            pref[KEY_START_MONTH] = startMonth
            pref[KEY_START_YEAR] = startYear
            pref[KEY_HISTORY_DEPTH] = depth
            pref[KEY_IS_FIRST_RUN] = false
        }
    }

    suspend fun setDefaultSalary(salary: Double) {
        context.dataStore.edit { it[KEY_DEFAULT_SALARY] = salary }
    }

    suspend fun setStartMonth(month: Int, year: Int) {
        context.dataStore.edit { pref ->
            pref[KEY_START_MONTH] = month
            pref[KEY_START_YEAR] = year
        }
    }

    suspend fun setHistoryDepth(depth: Int) {
        context.dataStore.edit { it[KEY_HISTORY_DEPTH] = depth }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setCurrency(currency: AppCurrency) {
        context.dataStore.edit { it[KEY_CURRENCY] = currency.name }
    }

    suspend fun setRateBank(bank: Bank) {
        context.dataStore.edit { it[KEY_RATE_BANK] = bank.name }
    }

    suspend fun saveRate(rate: ExchangeRate) {
        context.dataStore.edit { it[rateKey(rate.bank)] = rate.serialize() }
    }
}
