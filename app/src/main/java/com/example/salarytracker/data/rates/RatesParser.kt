package com.example.salarytracker.data.rates

import org.json.JSONArray
import org.json.JSONObject

// Пара курсов доллара: банк покупает / банк продает (BYN за 1 USD)
data class UsdRate(val buy: Double, val sell: Double)

class RatesParseException(message: String) : Exception(message)

/*
Разбор ответов банков. Чистые функции без сети, покрыты юнит-тестами.
*/
object RatesParser {
    private val SANE_RANGE = 0.5..20.0

    private fun pair(a: Double, b: Double): UsdRate {
        if (a !in SANE_RANGE || b !in SANE_RANGE) throw RatesParseException("Странный курс: $a / $b")
        return UsdRate(buy = minOf(a, b), sell = maxOf(a, b))
    }

    // api.nbrb.by/exrates/rates/USD?parammode=2
    fun parseNbrb(json: String): UsdRate {
        val o = JSONObject(json)
        val rate = o.getDouble("Cur_OfficialRate") / o.optInt("Cur_Scale", 1)
        return pair(rate, rate)
    }

    // belarusbank.by/api/kursExchange: массив отделений, берем первое с ненулевым курсом
    fun parseBelarusbank(json: String): UsdRate {
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val buy = o.optString("USD_in").toDoubleOrNull() ?: continue
            val sell = o.optString("USD_out").toDoubleOrNull() ?: continue
            if (buy > 0 && sell > 0) return pair(buy, sell)
        }
        throw RatesParseException("Нет курса USD у Беларусбанка")
    }

    // developerhub.alfabank.by:8273/partner/1.0.0/public/rates: пара USD/BYN
    fun parseAlfa(json: String): UsdRate {
        val rates = JSONObject(json).getJSONArray("rates")
        for (i in 0 until rates.length()) {
            val o = rates.getJSONObject(i)
            if (o.optString("sellIso") == "USD" && o.optString("buyIso") == "BYN") {
                return pair(o.getDouble("sellRate"), o.getDouble("buyRate"))
            }
        }
        throw RatesParseException("Нет курса USD у Альфа-Банка")
    }

    private val USD_ROW = Regex("""(?<![/A-Z])USD\s+(\d+[.,]\d+)\s+(\d+[.,]\d+)""")

    // bnb.by/kursy-valyut/fizicheskim-litsam/: открытого API нет, разбираем таблицы отделений.
    // Берем отделение в Минске, если найдется, иначе первое.
    fun parseBnbHtml(html: String): UsdRate {
        val text = html
            .replace(Regex("(?is)<script.*?</script>|<style.*?</style>"), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
        var previousEnd = 0
        var first: UsdRate? = null
        for (match in USD_ROW.findAll(text)) {
            val branchText = text.substring(previousEnd, match.range.first)
            previousEnd = match.range.last + 1
            val buy = match.groupValues[1].replace(',', '.').toDouble()
            val sell = match.groupValues[2].replace(',', '.').toDouble()
            if (buy <= 0 || sell <= 0) continue
            val rate = runCatching { pair(buy, sell) }.getOrNull() ?: continue
            if (branchText.contains("Минск")) return rate
            if (first == null) first = rate
        }
        return first ?: throw RatesParseException("Не удалось найти курс USD на странице БНБ")
    }
}
