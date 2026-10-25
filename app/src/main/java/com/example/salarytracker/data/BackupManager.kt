package com.example.salarytracker.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class BackupSettings(
    val defaultSalary: Double,
    val startMonth: Int,
    val startYear: Int,
    val historyDepth: Int,
    val themeMode: ThemeMode
)

data class BackupData(
    val createdAt: LocalDate,
    val settings: BackupSettings,
    val transactions: List<Transaction>,
    val salaryConfigs: List<SalaryConfig>,
    val projects: List<PendingProject>
)

class BackupFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/*
Резервная копия всех данных в JSON (платежи, оклады по месяцам, проекты в работе, настройки).
Формат версионируется полем formatVersion: старые копии должны открываться и в новых версиях приложения.
*/
object BackupManager {
    const val APP_ID = "SalaryTracker"
    const val FORMAT_VERSION = 1

    fun toJson(data: BackupData): String {
        val root = JSONObject()
        root.put("app", APP_ID)
        root.put("formatVersion", FORMAT_VERSION)
        root.put("createdAt", data.createdAt.toString())

        root.put("settings", JSONObject().apply {
            put("defaultSalary", data.settings.defaultSalary)
            put("startMonth", data.settings.startMonth)
            put("startYear", data.settings.startYear)
            put("historyDepth", data.settings.historyDepth)
            put("themeMode", data.settings.themeMode.name)
        })

        root.put("transactions", JSONArray().apply {
            data.transactions.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("amount", t.amount)
                    put("date", t.date.toString())
                    put("paymentType", t.paymentType)
                    put("isSideIncome", t.isSideIncome)
                    put("sourceNote", t.sourceNote ?: JSONObject.NULL)
                })
            }
        })

        root.put("salaryConfigs", JSONArray().apply {
            data.salaryConfigs.forEach { c ->
                put(JSONObject().apply {
                    put("monthYearKey", c.monthYearKey)
                    put("customSalary", c.customSalary)
                })
            }
        })

        root.put("projects", JSONArray().apply {
            data.projects.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("amount", p.amount)
                    put("date", p.date.toString())
                })
            }
        })

        return root.toString(2)
    }

    // Полностью разбирает и проверяет файл ДО того, как что-либо будет удалено из базы
    fun fromJson(json: String): BackupData {
        try {
            val root = JSONObject(json)
            if (root.optString("app") != APP_ID) {
                throw BackupFormatException("Это не резервная копия SalaryTracker")
            }
            val version = root.getInt("formatVersion")
            if (version > FORMAT_VERSION) {
                throw BackupFormatException("Копия создана в более новой версии приложения. Обновите приложение")
            }

            val s = root.getJSONObject("settings")
            val startMonth = s.getInt("startMonth")
            if (startMonth !in 1..12) throw BackupFormatException("Неверный месяц начала учета в копии")
            val settings = BackupSettings(
                defaultSalary = s.getDouble("defaultSalary"),
                startMonth = startMonth,
                startYear = s.getInt("startYear"),
                historyDepth = s.getInt("historyDepth").coerceAtLeast(1),
                themeMode = ThemeMode.fromName(s.optString("themeMode"))
            )

            val transactions = root.getJSONArray("transactions").objects().map { o ->
                Transaction(
                    id = o.getInt("id"),
                    amount = o.getDouble("amount"),
                    date = LocalDate.parse(o.getString("date")),
                    paymentType = o.getString("paymentType"),
                    isSideIncome = o.optBoolean("isSideIncome", false),
                    sourceNote = if (o.isNull("sourceNote")) null else o.optString("sourceNote")
                )
            }

            val salaryConfigs = root.getJSONArray("salaryConfigs").objects().map { o ->
                SalaryConfig(
                    monthYearKey = o.getString("monthYearKey"),
                    customSalary = o.getDouble("customSalary")
                )
            }

            val projects = root.getJSONArray("projects").objects().map { o ->
                PendingProject(
                    id = o.getInt("id"),
                    title = o.getString("title"),
                    amount = o.getDouble("amount"),
                    date = LocalDate.parse(o.getString("date"))
                )
            }

            return BackupData(
                createdAt = LocalDate.parse(root.getString("createdAt")),
                settings = settings,
                transactions = transactions,
                salaryConfigs = salaryConfigs,
                projects = projects
            )
        } catch (e: BackupFormatException) {
            throw e
        } catch (e: Exception) {
            throw BackupFormatException("Файл поврежден или имеет неверный формат", e)
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
