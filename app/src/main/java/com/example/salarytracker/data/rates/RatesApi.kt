package com.example.salarytracker.data.rates

import com.example.salarytracker.data.Bank
import com.example.salarytracker.data.ExchangeRate
import java.net.HttpURLConnection
import java.net.URL

// Загрузка курсов из сети. Вызывать только из фонового потока (Dispatchers.IO)
object RatesApi {
    private fun url(bank: Bank): String = when (bank) {
        Bank.NBRB -> "https://api.nbrb.by/exrates/rates/USD?parammode=2"
        Bank.BNB -> "https://bnb.by/kursy-valyut/fizicheskim-litsam/"
        Bank.BELARUSBANK -> "https://belarusbank.by/api/kursExchange?city=%D0%9C%D0%B8%D0%BD%D1%81%D0%BA"
        Bank.ALFA -> "https://developerhub.alfabank.by:8273/partner/1.0.0/public/rates"
    }

    private fun download(address: String): String {
        val connection = URL(address).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) SalaryTracker")
            connection.setRequestProperty("Accept", "*/*")
            val code = connection.responseCode
            if (code !in 200..299) throw RatesParseException("Сервер ответил $code")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    fun fetch(bank: Bank): ExchangeRate {
        val body = download(url(bank))
        val rate = when (bank) {
            Bank.NBRB -> RatesParser.parseNbrb(body)
            Bank.BNB -> RatesParser.parseBnbHtml(body)
            Bank.BELARUSBANK -> RatesParser.parseBelarusbank(body)
            Bank.ALFA -> RatesParser.parseAlfa(body)
        }
        return ExchangeRate(bank, rate.buy, rate.sell, System.currentTimeMillis())
    }
}
