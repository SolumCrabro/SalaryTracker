package com.example.salarytracker.data

enum class Bank(val title: String, val shortTitle: String) {
    NBRB("Нацбанк РБ (официальный)", "НБРБ"),
    BNB("БНБ-Банк", "БНБ"),
    BELARUSBANK("Беларусбанк", "Беларусбанк"),
    ALFA("Альфа-Банк", "Альфа-Банк");

    companion object {
        fun fromName(name: String?): Bank = entries.firstOrNull { it.name == name } ?: NBRB
    }
}

// Курс доллара в BYN за 1 USD. У Нацбанка один официальный курс: buy == sell
data class ExchangeRate(
    val bank: Bank,
    val buy: Double,
    val sell: Double,
    val updatedAtMillis: Long
) {
    fun serialize(): String = "$buy;$sell;$updatedAtMillis"

    companion object {
        fun deserialize(bank: Bank, value: String?): ExchangeRate? {
            val parts = value?.split(';') ?: return null
            if (parts.size != 3) return null
            return ExchangeRate(
                bank,
                parts[0].toDoubleOrNull() ?: return null,
                parts[1].toDoubleOrNull() ?: return null,
                parts[2].toLongOrNull() ?: return null
            )
        }
    }
}
