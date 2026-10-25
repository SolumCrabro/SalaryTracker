package com.example.salarytracker.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

// Валюта учёта: в ней хранятся все суммы (платежи, оклады, проекты)
enum class AppCurrency(val code: String, val title: String) {
    USD("USD", "Доллар США ($)"),
    BYN("BYN", "Белорусский рубль (BYN)");

    // Валюта, которую можно вносить через конвертер
    val other: AppCurrency get() = if (this == USD) BYN else USD

    // Короткое обозначение рядом с суммой: "$" или "BYN"
    val label: String get() = if (this == USD) "$" else "BYN"

    companion object {
        fun fromName(name: String?): AppCurrency = entries.firstOrNull { it.name == name } ?: USD
    }
}

private fun amountText(value: Double): String {
    val rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
    return if (rounded.stripTrailingZeros().scale() <= 0) {
        String.format(Locale.US, "%,.0f", rounded)
    } else {
        String.format(Locale.US, "%,.2f", rounded).trimEnd('0').trimEnd('.')
    }
}

// "$1,500", "$169.5", "1,500 BYN"
fun formatMoney(value: Double, currency: AppCurrency): String =
    if (currency == AppCurrency.USD) "$${amountText(value)}" else "${amountText(value)} BYN"

/*
Пересчет суммы во валюту учёта по курсу банка (BYN за 1 USD):
- учёт в $, вносим BYN: банк продает доллары -> BYN / курс продажи;
- учёт в BYN, вносим $: банк покупает доллары -> USD * курс покупки.
Итог округляется до десятых.
*/
object CurrencyConverter {
    fun rateFor(target: AppCurrency, rate: ExchangeRate): Double =
        if (target == AppCurrency.USD) rate.sell else rate.buy

    fun convert(amount: Double, target: AppCurrency, rate: ExchangeRate): Double {
        val r = rateFor(target, rate)
        val raw = if (target == AppCurrency.USD) amount / r else amount * r
        return BigDecimal.valueOf(raw).setScale(1, RoundingMode.HALF_UP).toDouble()
    }
}

// Курс без лишних нулей: 2.95, 2.9543
fun formatRate(rate: Double): String = String.format(Locale.US, "%.4f", rate).trimEnd('0').trimEnd('.')
