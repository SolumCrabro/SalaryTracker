package com.example.salarytracker.data.rates

import org.junit.Assert.assertEquals
import org.junit.Test

class RatesParserTest {
    @Test
    fun nbrb() {
        val json = """{"Cur_ID":431,"Cur_Abbreviation":"USD","Cur_Scale":1,"Cur_OfficialRate":2.9543}"""
        assertEquals(UsdRate(2.9543, 2.9543), RatesParser.parseNbrb(json))
    }

    @Test
    fun belarusbankSkipsEmptyBranches() {
        val json = """[{"USD_in":"0.0000","USD_out":"0.0000"},{"USD_in":"2.9350","USD_out":"2.9850"}]"""
        assertEquals(UsdRate(2.935, 2.985), RatesParser.parseBelarusbank(json))
    }

    @Test
    fun alfaPicksUsdByn() {
        val json = """{"rates":[
            {"sellRate":77.0,"sellIso":"USD","buyRate":86.0,"buyIso":"RUB"},
            {"sellRate":2.9,"sellIso":"USD","buyRate":3.0,"buyIso":"BYN"}]}"""
        assertEquals(UsdRate(2.9, 3.0), RatesParser.parseAlfa(json))
    }

    @Test
    fun bnbPrefersMinskBranchAndIgnoresCrossRates() {
        val html = """
            <table><tr><td>EUR/USD</td><td>1.1065</td><td>1.1295</td></tr></table>
            <div>Центр банковских услуг №14 Могилев, ул. Ленинская</div>
            <table><tr><td>USD</td><td> 2.9355 </td><td> 2.9575 </td></tr></table>
            <div>Центр банковских услуг №1 Минск, пр. Независимости</div>
            <table><tr><td>USD</td><td>2.9405</td><td>2.9540</td></tr></table>
        """
        assertEquals(UsdRate(2.9405, 2.954), RatesParser.parseBnbHtml(html))
    }

    @Test
    fun bnbFallsBackToFirstBranch() {
        val html = "<p>Гомель</p><td>USD</td><td>2.9350</td><td>2.9550</td><p>Брест</p><td>USD</td><td>2.94</td><td>2.96</td>"
        assertEquals(UsdRate(2.935, 2.955), RatesParser.parseBnbHtml(html))
    }

    @Test(expected = RatesParseException::class)
    fun bnbWithoutRatesFails() {
        RatesParser.parseBnbHtml("<html>Технические работы</html>")
    }
}
