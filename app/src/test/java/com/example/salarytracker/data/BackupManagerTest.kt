package com.example.salarytracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class BackupManagerTest {

    private val sample = BackupData(
        createdAt = LocalDate.of(2026, 10, 25),
        settings = BackupSettings(
            defaultSalary = 1500.0,
            startMonth = 11,
            startYear = 2025,
            historyDepth = 3,
            themeMode = ThemeMode.DARK
        ),
        transactions = listOf(
            Transaction(id = 1, amount = 1000.0, date = LocalDate.of(2025, 12, 5), paymentType = "CARD"),
            Transaction(
                id = 2, amount = 300.5, date = LocalDate.of(2026, 1, 3), paymentType = "CASH",
                isSideIncome = true, sourceNote = "Сайт \"Ромашка\"", projectId = 7
            )
        ),
        salaryConfigs = listOf(SalaryConfig("DECEMBER_2025", 2000.0)),
        projects = listOf(PendingProject(id = 7, title = "Ремонт", amount = 450.0, date = LocalDate.of(2026, 2, 1)))
    )

    @Test
    fun roundTripKeepsAllData() {
        val restored = BackupManager.fromJson(BackupManager.toJson(sample))
        assertEquals(sample, restored)
    }

    @Test
    fun formatVersion1WithoutProjectIdIsAccepted() {
        val root = org.json.JSONObject(BackupManager.toJson(sample))
        root.put("formatVersion", 1)
        val transactions = root.getJSONArray("transactions")
        for (i in 0 until transactions.length()) transactions.getJSONObject(i).remove("projectId")
        val v1 = root.toString()
        val restored = BackupManager.fromJson(v1)
        assertEquals(sample.transactions.map { it.copy(projectId = null) }, restored.transactions)
    }

    @Test
    fun foreignJsonIsRejected() {
        assertThrows(BackupFormatException::class.java) {
            BackupManager.fromJson("""{"app":"Other","formatVersion":1}""")
        }
    }

    @Test
    fun brokenFileIsRejected() {
        assertThrows(BackupFormatException::class.java) {
            BackupManager.fromJson("not a json")
        }
    }

    @Test
    fun newerFormatVersionIsRejected() {
        val json = BackupManager.toJson(sample)
            .replace(Regex("\"formatVersion\":\\s*\\d+"), "\"formatVersion\": 99")
        assertThrows(BackupFormatException::class.java) {
            BackupManager.fromJson(json)
        }
    }
}
