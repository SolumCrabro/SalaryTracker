package com.example.salarytracker.ui.screens

import com.example.salarytracker.ui.theme.accentDark
import com.example.salarytracker.ui.theme.appAccentColor
import com.example.salarytracker.ui.theme.appCurrency
import com.example.salarytracker.ui.theme.money
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.data.AppCurrency
import com.example.salarytracker.data.AccentColor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import com.example.salarytracker.data.BackupData
import com.example.salarytracker.data.Bank
import com.example.salarytracker.data.formatRate
import androidx.compose.material.icons.filled.Refresh
import com.example.salarytracker.data.ThemeMode
import com.example.salarytracker.ui.components.BackupRestoreDialog
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ruLocale = Locale("ru")

private fun monthTitle(date: LocalDate): String =
    "${date.month.getDisplayName(TextStyle.FULL_STANDALONE, ruLocale).replaceFirstChar { it.uppercase() }} ${date.year}"

private fun depthTitle(depth: Int): String = when (depth) {
    1 -> "1 месяц"
    in 2..4 -> "$depth месяца"
    else -> "$depth месяцев"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SalaryViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()

    val defaultSalary by viewModel.defaultSalaryState.collectAsState()
    val startDate by viewModel.startDateState.collectAsState()
    val historyDepth by viewModel.historyDepthState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val canChangeCurrency by viewModel.canChangeCurrency.collectAsState()
    val accent by viewModel.accentColor.collectAsState()
    val rateBank by viewModel.rateBank.collectAsState()
    val rates by viewModel.cachedRates.collectAsState()
    val loadingRates by viewModel.loadingRates.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    val showRateError: (String) -> Unit = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

    // Курсы, которых еще нет, подтягиваются при открытии настроек
    LaunchedEffect(Unit) {
        Bank.entries.filter { it !in rates }.forEach { viewModel.refreshRate(it) }
    }

    var showSalaryDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }
    var showDepthDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<BackupData?>(null) }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val labelTextColor = Color(0xFF7A7D84)
    val accentColor = appAccentColor()
    val dialogBg = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF)
    val borderStroke = if (isDark) {
        BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.03f))))
    } else {
        BorderStroke(1.5.dp, accentDark)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri) { _, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.readBackup(uri) { result ->
                result
                    .onSuccess { pendingRestore = it }
                    .onFailure {
                        Toast.makeText(context, it.message ?: "Файл не похож на копию SalaryTracker", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    @Composable
    fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelTextColor,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(borderStroke, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
        ) {
            Column(content = content)
        }
    }

    @Composable
    fun SettingRow(title: String, value: String?, subtitle: String? = null, onClick: (() -> Unit)?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, color = mainTextColor)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 13.sp, color = labelTextColor)
                }
            }
            if (value != null) {
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
            }
        }
    }

    @Composable
    fun RowDivider() {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = mainTextColor
                    )
                }
                Text(text = "Настройки", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = mainTextColor)
            }

            SectionCard("Учёт зарплаты") {
                SettingRow(
                    title = "Оклад по умолчанию",
                    value = defaultSalary?.let { money(it) },
                    onClick = { showSalaryDialog = true }
                )
                RowDivider()
                SettingRow(
                    title = "Начало учёта",
                    value = startDate?.let { monthTitle(it) },
                    onClick = { showStartDialog = true }
                )
                RowDivider()
                SettingRow(
                    title = "Месяцев на главной",
                    value = historyDepth?.let { depthTitle(it) },
                    onClick = { showDepthDialog = true }
                )
                RowDivider()
                SettingRow(
                    title = "Валюта учёта",
                    value = currency.code,
                    subtitle = if (canChangeCurrency) null else "Выбирается при первом запуске. Изменить можно, пока нет платежей и проектов",
                    onClick = if (canChangeCurrency) ({ showCurrencyDialog = true }) else null
                )
            }

            SectionCard("Курсы валют") {
                SettingRow(
                    title = "Банк для конвертера",
                    value = rateBank.shortTitle,
                    onClick = { showBankDialog = true }
                )
                RowDivider()
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "USD → BYN", fontSize = 13.sp, color = labelTextColor, modifier = Modifier.weight(1f))
                    Text(text = "покупка / продажа", fontSize = 13.sp, color = labelTextColor)
                    IconButton(
                        onClick = { Bank.entries.forEach { viewModel.refreshRate(it, showRateError) } },
                        enabled = loadingRates.isEmpty()
                    ) {
                        if (loadingRates.isEmpty()) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Обновить курсы", tint = accentColor)
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accentColor)
                        }
                    }
                }
                Bank.entries.forEach { bank ->
                    val rate = rates[bank]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateRateBank(bank) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bank.shortTitle,
                            fontSize = 15.sp,
                            fontWeight = if (bank == rateBank) FontWeight.Bold else FontWeight.Normal,
                            color = if (bank == rateBank) accentColor else mainTextColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = when {
                                rate == null -> "—"
                                rate.buy == rate.sell -> formatRate(rate.buy)
                                else -> "${formatRate(rate.buy)} / ${formatRate(rate.sell)}"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = mainTextColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = "При внесении рублей при учёте в долларах берётся курс продажи, при внесении долларов при учёте в рублях — курс покупки. У Нацбанка один официальный курс. Курс БНБ берётся со страницы сайта банка.",
                fontSize = 13.sp,
                color = labelTextColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            SectionCard("Внешний вид") {
                SettingRow(title = "Тема", value = themeMode.title, onClick = { showThemeDialog = true })
                RowDivider()
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Цвет акцента", fontSize = 16.sp, color = mainTextColor, modifier = Modifier.weight(1f))
                        Text(text = accent.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccentColor.entries.forEach { option ->
                            val swatch = Color(if (isDark) option.dark else option.light)
                            val isSelected = option == accent
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, if (isSelected) swatch else Color.Transparent, CircleShape)
                                    .padding(4.dp)
                                    .background(swatch, CircleShape)
                                    .clickable { viewModel.updateAccentColor(option) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = option.title,
                                        tint = if (isDark) Color(0xFF111214) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SectionCard("Резервная копия") {
                SettingRow(
                    title = "Сохранить в файл",
                    value = null,
                    onClick = { exportLauncher.launch("SalaryTracker_${LocalDate.now()}.json") }
                )
                RowDivider()
                SettingRow(
                    title = "Восстановить из файла",
                    value = null,
                    onClick = { importLauncher.launch(BACKUP_MIME_TYPES) }
                )
            }
            Text(
                text = "Копия хранит платежи, оклады по месяцам, проекты и настройки. Сохрани её перед переустановкой или сменой телефона.",
                fontSize = 13.sp,
                color = labelTextColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }

    if (showSalaryDialog) {
        var salaryText by remember { mutableStateOf(defaultSalary?.toLong()?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showSalaryDialog = false },
            containerColor = dialogBg,
            titleContentColor = mainTextColor,
            title = { Text("Оклад по умолчанию", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = salaryText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) salaryText = it },
                        label = { Text("Сумма (${appCurrency().label})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor,
                            focusedTextColor = mainTextColor,
                            unfocusedTextColor = mainTextColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Действует с текущего месяца. Прошлые месяцы сохранят прежний оклад.",
                        fontSize = 13.sp,
                        color = labelTextColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = salaryText.toDoubleOrNull()
                        if (value != null && value > 0) {
                            viewModel.updateDefaultSalary(value)
                            showSalaryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Сохранить", color = if (isDark) Color(0xFF111214) else Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSalaryDialog = false }) { Text("Отмена", color = accentColor) }
            }
        )
    }

    if (showStartDialog) {
        val now = LocalDate.now().withDayOfMonth(1)
        val options = (0L until 24L).map { now.minusMonths(it) }
        OptionsDialog(
            title = "Начало учёта",
            hint = "Долг считается с первого дня выбранного месяца.",
            options = options,
            selected = startDate,
            label = ::monthTitle,
            onSelect = {
                viewModel.updateStartMonth(it)
                showStartDialog = false
            },
            onDismiss = { showStartDialog = false }
        )
    }

    if (showDepthDialog) {
        OptionsDialog(
            title = "Месяцев на главной",
            hint = "Остальные прошедшие месяцы можно развернуть под списком. Общий долг считается за всё время.",
            options = listOf(1, 2, 3, 6, 12),
            selected = historyDepth,
            label = ::depthTitle,
            onSelect = {
                viewModel.updateHistoryDepth(it)
                showDepthDialog = false
            },
            onDismiss = { showDepthDialog = false }
        )
    }

    if (showThemeDialog) {
        OptionsDialog(
            title = "Тема",
            hint = null,
            options = ThemeMode.entries.toList(),
            selected = themeMode,
            label = { it.title },
            onSelect = {
                viewModel.updateThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showCurrencyDialog && canChangeCurrency) {
        OptionsDialog(
            title = "Валюта учёта",
            hint = "После первого платежа или проекта валюту изменить будет нельзя.",
            options = AppCurrency.entries.toList(),
            selected = currency,
            label = { it.title },
            onSelect = {
                viewModel.updateCurrency(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showBankDialog) {
        OptionsDialog(
            title = "Банк для конвертера",
            hint = "Курс этого банка используется при внесении средств в другой валюте.",
            options = Bank.entries.toList(),
            selected = rateBank,
            label = { it.title },
            onSelect = {
                viewModel.updateRateBank(it)
                showBankDialog = false
            },
            onDismiss = { showBankDialog = false }
        )
    }

    pendingRestore?.let { backup ->
        BackupRestoreDialog(
            backup = backup,
            onConfirm = {
                pendingRestore = null
                viewModel.restoreBackup(backup) { _, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { pendingRestore = null }
        )
    }
}

// Некоторые файловые менеджеры не знают тип .json, поэтому разрешаем и общие типы
val BACKUP_MIME_TYPES = arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")

@Composable
private fun <T> OptionsDialog(
    title: String,
    hint: String?,
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val accentColor = appAccentColor()
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF),
        titleContentColor = mainTextColor,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (hint != null) {
                    Text(text = hint, fontSize = 13.sp, color = Color(0xFF7A7D84))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label(option), fontSize = 15.sp, color = mainTextColor)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = accentColor) }
        }
    )
}
