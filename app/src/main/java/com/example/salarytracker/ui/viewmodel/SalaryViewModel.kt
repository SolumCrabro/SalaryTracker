package com.example.salarytracker.ui.viewmodel

/*
Этот класс будет принимать нашу базу данных (TransactionDao),
предоставлять список всех транзакций и содержать функцию для
сохранения новой записи.
*/
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.salarytracker.data.AppCurrency
import com.example.salarytracker.data.AppDatabase
import com.example.salarytracker.data.Bank
import com.example.salarytracker.data.ExchangeRate
import com.example.salarytracker.data.rates.RatesApi
import com.example.salarytracker.data.AppSettings
import com.example.salarytracker.data.BackupData
import com.example.salarytracker.data.BackupFormatException
import com.example.salarytracker.data.BackupManager
import com.example.salarytracker.data.BackupSettings
import com.example.salarytracker.data.PendingProject
import com.example.salarytracker.data.SalaryConfig
import com.example.salarytracker.data.ThemeMode
import com.example.salarytracker.data.Transaction
import com.example.salarytracker.domain.MonthSummary
import com.example.salarytracker.domain.ProjectProgress
import com.example.salarytracker.domain.SalaryCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val appSettings = AppSettings(application)

    val isFirstRun = appSettings.isFirstRun.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val defaultSalary = appSettings.defaultSalary
    private val startMonth = appSettings.startMonth
    private val startYear = appSettings.startYear
    private val historyDepth = appSettings.historyDepth

    // --- НАСТРОЙКИ ДЛЯ ЭКРАНА "НАСТРОЙКИ" ---
    val themeMode: StateFlow<ThemeMode> = appSettings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val currency: StateFlow<AppCurrency> = appSettings.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppCurrency.USD)

    // --- КУРСЫ ВАЛЮТ ---
    val rateBank: StateFlow<Bank> = appSettings.rateBank
        .stateIn(viewModelScope, SharingStarted.Eagerly, Bank.NBRB)
    val cachedRates: StateFlow<Map<Bank, ExchangeRate>> = appSettings.cachedRates
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    private val _loadingRates = MutableStateFlow<Set<Bank>>(emptySet())
    val loadingRates: StateFlow<Set<Bank>> = _loadingRates.asStateFlow()

    fun refreshRate(bank: Bank, onError: (String) -> Unit = {}) {
        if (bank in _loadingRates.value) return
        _loadingRates.update { it + bank }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { RatesApi.fetch(bank).also { appSettings.saveRate(it) } }
            }
            _loadingRates.update { it - bank }
            result.onFailure { onError("Не удалось обновить курс ${bank.shortTitle}. Проверьте интернет") }
        }
    }

    fun updateRateBank(bank: Bank) {
        viewModelScope.launch(Dispatchers.IO) { appSettings.setRateBank(bank) }
    }

    fun updateCurrency(currency: AppCurrency) {
        viewModelScope.launch(Dispatchers.IO) { appSettings.setCurrency(currency) }
    }

    val defaultSalaryState: StateFlow<Double?> = defaultSalary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val startDateState: StateFlow<LocalDate?> = combine(startMonth, startYear) { m, y -> LocalDate.of(y, m, 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val historyDepthState: StateFlow<Int?> = historyDepth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSalaryConfigs: Flow<List<SalaryConfig>> = transactionDao.getAllSalaryConfigs()

    val allPendingProjects: StateFlow<List<PendingProject>> = transactionDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Проекты в работе с их частичными оплатами
    val projectProgress: StateFlow<List<ProjectProgress>> = combine(
        allPendingProjects, allTransactions
    ) { projects, transactions ->
        ProjectProgress.build(projects, transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Все месяцы от начала учета до текущего, в хронологическом порядке
    val allActiveSummaries: StateFlow<List<MonthSummary>> = combine(
        allTransactions, allSalaryConfigs, defaultSalary, startMonth, startYear
    ) { transactions, configs, defSalary, stMonth, stYear ->
        SalaryCalculator.calculate(
            // Считаем сумму ТОЛЬКО основных выплат зарплаты (исключаем "левые приходы" / подработки)
            mainIncomeTotal = transactions.filter { !it.isSideIncome }.sumOf { it.amount },
            salaryOverrides = configs.associate { it.monthYearKey to it.customSalary },
            defaultSalary = defSalary,
            startMonth = stMonth,
            startYear = stYear,
            today = LocalDate.now()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Последние N месяцев (N = глубина истории), от новых к старым
    val monthlySummaries: StateFlow<List<MonthSummary>> = combine(
        allActiveSummaries, historyDepth
    ) { summaries, depth ->
        summaries.takeLast(depth).reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Остальные учтенные месяцы старше N, от новых к старым (раскрываются на главной)
    val olderSummaries: StateFlow<List<MonthSummary>> = combine(
        allActiveSummaries, historyDepth
    ) { summaries, depth ->
        summaries.dropLast(depth).filter { it.isActive }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completeOnboarding(salary: Double, startMonth: Int, startYear: Int, depth: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.saveInitialSettings(salary, startMonth, startYear, depth)
        }
    }

    // amount — в валюте учёта; original* заполняются, если сумма внесена через конвертер
    fun addTransaction(
        amount: Double,
        date: LocalDate,
        paymentType: String,
        originalAmount: Double? = null,
        originalCurrency: AppCurrency? = null,
        exchangeRate: Double? = null,
        rateSource: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertTransaction(
                Transaction(
                    amount = amount,
                    date = date,
                    paymentType = paymentType,
                    originalAmount = originalAmount,
                    originalCurrency = originalCurrency?.name,
                    exchangeRate = exchangeRate,
                    rateSource = rateSource
                )
            )
        }
    }

    fun updateSalaryForMonth(dbKey: String, newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertSalaryConfig(SalaryConfig(dbKey, newSalary))
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction)
        }
    }

    // --- МЕТОДЫ ДЛЯ ПРОЕКТОВ В РАБОТЕ ---
    fun addPendingProject(title: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertProject(
                PendingProject(title = title, amount = amount)
            )
        }
    }

    private fun projectPayment(project: PendingProject, amount: Double, paymentType: String) = Transaction(
        amount = amount,
        date = LocalDate.now(),
        paymentType = paymentType,
        isSideIncome = true, // "левый приход", в долг по зарплате не идет
        sourceNote = project.title,
        projectId = project.id
    )

    // Частичная оплата проекта; если набралась вся сумма — проект закрывается
    fun addProjectPayment(project: PendingProject, amount: Double, paymentType: String) {
        if (amount <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                val paid = transactionDao.getProjectPaidSum(project.id)
                transactionDao.insertTransaction(projectPayment(project, amount, paymentType))
                if (ProjectProgress.isClosed(project.amount, paid + amount)) {
                    transactionDao.deleteProject(project)
                }
            }
        }
    }

    // Закрыть полностью: зачисляется весь остаток, проект убирается из "в работе"
    fun completeProject(project: PendingProject, paymentType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                val remaining = project.amount - transactionDao.getProjectPaidSum(project.id)
                if (!ProjectProgress.isClosed(project.amount, project.amount - remaining)) {
                    transactionDao.insertTransaction(projectPayment(project, remaining, paymentType))
                }
                transactionDao.deleteProject(project)
            }
        }
    }

    fun deletePendingProject(project: PendingProject) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteProject(project)
        }
    }

    // --- МЕТОДЫ ДЛЯ ЭКРАНА НАСТРОЕК ---

    /*
    Новый оклад действует с текущего месяца: прошлым месяцам без ручной зарплаты
    сначала фиксируется старый оклад, чтобы их долг не пересчитался задним числом.
    */
    fun updateDefaultSalary(newSalary: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldSalary = defaultSalary.first()
            if (oldSalary == newSalary) return@launch

            val overriddenKeys = transactionDao.getAllSalaryConfigsOnce().map { it.monthYearKey }.toSet()
            val currentFirstDay = LocalDate.now().withDayOfMonth(1)
            var month = LocalDate.of(startYear.first(), startMonth.first(), 1)
            val frozenMonths = mutableListOf<SalaryConfig>()
            while (month.isBefore(currentFirstDay)) {
                val key = SalaryCalculator.monthKey(month)
                if (key !in overriddenKeys) frozenMonths.add(SalaryConfig(key, oldSalary))
                month = month.plusMonths(1)
            }
            database.withTransaction {
                transactionDao.insertSalaryConfigs(frozenMonths)
            }
            appSettings.setDefaultSalary(newSalary)
        }
    }

    fun updateStartMonth(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.setStartMonth(date.monthValue, date.year)
        }
    }

    fun updateHistoryDepth(depth: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.setHistoryDepth(depth)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            appSettings.setThemeMode(mode)
        }
    }

    // --- РЕЗЕРВНАЯ КОПИЯ ---

    fun exportBackup(uri: Uri, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val data = BackupData(
                        createdAt = LocalDate.now(),
                        settings = BackupSettings(
                            defaultSalary = defaultSalary.first(),
                            startMonth = startMonth.first(),
                            startYear = startYear.first(),
                            historyDepth = historyDepth.first(),
                            themeMode = appSettings.themeMode.first(),
                            currency = appSettings.currency.first()
                        ),
                        transactions = transactionDao.getAllTransactionsOnce(),
                        salaryConfigs = transactionDao.getAllSalaryConfigsOnce(),
                        projects = transactionDao.getAllProjectsOnce()
                    )
                    val stream = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                        ?: error("Не удалось открыть файл для записи")
                    stream.bufferedWriter(Charsets.UTF_8).use { it.write(BackupManager.toJson(data)) }
                    data
                }
            }
            result
                .onSuccess { onResult(true, "Копия сохранена: платежей — ${it.transactions.size}, проектов — ${it.projects.size}") }
                .onFailure { onResult(false, "Не удалось сохранить копию: ${it.message ?: "ошибка записи"}") }
        }
    }

    // Читает и проверяет файл, ничего не меняя в базе
    fun readBackup(uri: Uri, onResult: (Result<BackupData>) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: throw BackupFormatException("Не удалось открыть файл")
                    val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    BackupManager.fromJson(text)
                }
            }
            onResult(result)
        }
    }

    // Полностью заменяет текущие данные данными из копии
    fun restoreBackup(data: BackupData, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    database.withTransaction {
                        transactionDao.deleteAllTransactions()
                        transactionDao.deleteAllSalaryConfigs()
                        transactionDao.deleteAllProjects()
                        // Оплаты уже закрытых проектов отвязываем, чтобы новый проект с тем же id их не "унаследовал"
                        val projectIds = data.projects.map { it.id }.toSet()
                        transactionDao.insertTransactions(
                            data.transactions.map { if (it.projectId in projectIds) it else it.copy(projectId = null) }
                        )
                        transactionDao.insertSalaryConfigs(data.salaryConfigs)
                        transactionDao.insertProjects(data.projects)
                    }
                    with(data.settings) {
                        appSettings.saveInitialSettings(defaultSalary, startMonth, startYear, historyDepth)
                        appSettings.setThemeMode(themeMode)
                        appSettings.setCurrency(currency)
                    }
                }
            }
            result
                .onSuccess { onResult(true, "Данные восстановлены") }
                .onFailure { onResult(false, "Не удалось восстановить: ${it.message ?: "ошибка базы данных"}") }
        }
    }
}
