package com.example.salarytracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyTest {
    private val rate = ExchangeRate(Bank.BNB, buy = 2.93, sell = 2.95, updatedAtMillis = 0)

    @Test
    fun bynToUsdUsesSellRateAndRoundsToTenths() {
        assertEquals(169.5, CurrencyConverter.convert(500.0, AppCurrency.USD, rate), 0.0001)
        assertEquals(2.95, CurrencyConverter.rateFor(AppCurrency.USD, rate), 0.0001)
    }

    @Test
    fun usdToBynUsesBuyRate() {
        assertEquals(293.0, CurrencyConverter.convert(100.0, AppCurrency.BYN, rate), 0.0001)
        assertEquals(44.0, CurrencyConverter.convert(15.0, AppCurrency.BYN, rate), 0.0001)
    }

    @Test
    fun moneyFormat() {
        assertEquals("$1,500", formatMoney(1500.0, AppCurrency.USD))
        assertEquals("$169.5", formatMoney(169.5, AppCurrency.USD))
        assertEquals("$33.33", formatMoney(33.333, AppCurrency.USD))
        assertEquals("2,000 BYN", formatMoney(2000.0, AppCurrency.BYN))
        assertEquals("2.95", formatRate(2.95))
        assertEquals("2.9543", formatRate(2.9543))
    }

    @Test
    fun rateCacheRoundTrip() {
        assertEquals(rate, ExchangeRate.deserialize(Bank.BNB, rate.serialize()))
        assertEquals(null, ExchangeRate.deserialize(Bank.BNB, "мусор"))
        assertEquals(AppCurrency.USD, AppCurrency.fromName(null))
        assertEquals(Bank.NBRB, Bank.fromName(null))
    }
}
