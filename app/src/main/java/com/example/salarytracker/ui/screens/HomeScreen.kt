package com.example.salarytracker.ui.screens

import com.example.salarytracker.ui.theme.appCurrency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Добавлен импорт для работы анимации сдвига
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.components.CurrentMonthCard
import com.example.salarytracker.ui.components.MonthRowItem
import com.example.salarytracker.domain.MonthSummary
import com.example.salarytracker.domain.SalaryCalculator
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // Импорт для запуска корутин

@Composable
fun HomeScreen(viewModel: SalaryViewModel = viewModel(), onOpenSettings: () -> Unit = {}) {
    val monthlyData by viewModel.monthlySummaries.collectAsState()
    val allSummaries by viewModel.allActiveSummaries.collectAsState()
    val olderMonths by viewModel.olderSummaries.collectAsState()
    var showOlderMonths by rememberSaveable { mutableStateOf(false) }
    val isDark = isAppInDarkTheme()
    val localCoroutineScope = rememberCoroutineScope() // Создали область для анимации списка

    val currentMonthInfo = allSummaries.find { it.isCurrentMonth }
    val currentMonthName = currentMonthInfo?.monthName ?: "Текущий"

    // Долг и переплата считаются по ВСЕМ месяцам с начала учета, а не только по видимым в истории
    val finalCardBalance = SalaryCalculator.totalBalance(allSummaries)

    var showDialog by remember { mutableStateOf(false) }
    var selectedMonthSummary by remember { mutableStateOf<MonthSummary?>(null) }
    var inputSalaryText by remember { mutableStateOf("") }

    // Состояние для запуска анимации при старте экрана
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val onMonthClick: (MonthSummary) -> Unit = { summary ->
        selectedMonthSummary = summary
        inputSalaryText = summary.totalSalary.toInt().toString()
        showDialog = true
    }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val accentColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)
    val headerTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мои деньги",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        color = headerTextColor
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = Color(0xFF7A7D84)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(animationSpec = tween(600), initialOffsetY = { it / 3 })
            ) {
                CurrentMonthCard(monthName = currentMonthName, totalDebt = finalCardBalance)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 150))
            ) {
                Text(
                    text = "ИСТОРИЯ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = headerTextColor
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 200))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Месяц", fontSize = 12.sp, color = Color(0xFF7A7D84), modifier = Modifier.weight(1.3f))
                    Spacer(modifier = Modifier.weight(0.5f))
                    Text(text = "Остаток", fontSize = 12.sp, color = Color(0xFF7A7D84), textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
                    Text(text = "Зарплата", fontSize = 12.sp, color = Color(0xFF7A7D84), textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(monthlyData) { index, summary ->

                    val itemAlpha = remember { Animatable(0f) }
                    val itemOffsetY = remember { Animatable(50f) }

                    LaunchedEffect(monthlyData) {
                        delay(250L + (index * 80L))

                        // ИСПРАВЛЕНИЕ: Используем localCoroutineScope для безопасного вызова launch и анимаций
                        localCoroutineScope.launch {
                            itemAlpha.animateTo(1f, animationSpec = tween(400))
                        }
                        localCoroutineScope.launch {
                            itemOffsetY.animateTo(0f, animationSpec = tween(400))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = itemAlpha.value
                                translationY = itemOffsetY.value
                            }
                            .clickable(enabled = summary.isActive) { onMonthClick(summary) }
                    ) {
                        HomeMonthRow(summary)
                    }
                }

                // Прошедшие месяцы старше глубины истории — для ознакомления, раскрываются по кнопке
                if (olderMonths.isNotEmpty()) {
                    item(key = "older_toggle") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { showOlderMonths = !showOlderMonths }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showOlderMonths) "Скрыть прошедшие месяцы" else "Показать все прошедшие (${olderMonths.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showOlderMonths) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = accentColor
                            )
                        }
                    }

                    if (showOlderMonths) {
                        items(olderMonths, key = { it.dbKey }) { summary ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMonthClick(summary) }
                            ) {
                                HomeMonthRow(summary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedMonthSummary != null) {
        val dialogBg = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF)
        val dialogTitle = if (isDark) Color.White else Color(0xFF111214)
        val dialogButtonColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = dialogBg,
            titleContentColor = dialogTitle,
            textContentColor = Color(0xFF7A7D84),
            title = { Text(text = "Зарплата за ${selectedMonthSummary!!.monthName} ${selectedMonthSummary!!.year}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // ИСПРАВЛЕНИЕ: Восстановлено слово Text вместо сломанного ext
                    Text(text = "Укажите плановый размер зарплаты на этот месяц:", fontSize = 14.sp, color = Color(0xFF7A7D84))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputSalaryText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) inputSalaryText = newValue
                        },
                        label = { Text("Сумма (${appCurrency().label})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dialogButtonColor,
                            focusedLabelColor = dialogButtonColor,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                            unfocusedLabelColor = Color(0xFF7A7D84),
                            focusedTextColor = dialogTitle,
                            unfocusedTextColor = dialogTitle
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newSalary = inputSalaryText.toDoubleOrNull()
                        if (newSalary != null && newSalary >= 0) {
                            viewModel.updateSalaryForMonth(selectedMonthSummary!!.dbKey, newSalary)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dialogButtonColor)
                ) {
                    Text("Сохранить", color = if (isDark) Color(0xFF111214) else Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена", color = dialogButtonColor)
                }
            }
        )
    }
}

@Composable
private fun HomeMonthRow(summary: MonthSummary) {
    val displayDebt = if (summary.surplus > 0) 0.0 else summary.debt
    MonthRowItem(
        monthName = summary.monthName,
        subTitle = summary.year.toString(),
        debt = displayDebt,
        salary = summary.totalSalary,
        isCurrentMonth = summary.isCurrentMonth,
        isActive = summary.isActive
    )
}
