package com.example.salarytracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.R
import com.example.salarytracker.ui.components.CurrentMonthCard
import com.example.salarytracker.ui.components.MonthRowItem
import com.example.salarytracker.ui.viewmodel.MonthSummary
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

@Composable
fun HomeScreen(viewModel: SalaryViewModel = viewModel()) {
    // Подписываемся на наш расчетный поток из ViewModel
    val monthlyData by viewModel.monthlySummaries.collectAsState()

    // Находим текущий месяц для отображения его имени в карточке
    val currentMonthInfo = monthlyData.find { it.isCurrentMonth }
    val currentMonthName = currentMonthInfo?.monthName ?: "Текущий"

    // Суммируем долг (Остаток) по всей таблице активных месяцев учета
    val activeMonths = monthlyData.filter { it.isActive }
    val totalDebtValue = activeMonths.sumOf { it.debt }
    val currentMonthSurplus = currentMonthInfo?.surplus ?: 0.0

    val finalCardBalance = if (currentMonthSurplus > 0) -currentMonthSurplus else totalDebtValue

    // Состояния для управления диалоговым окном редактирования зарплаты
    var showDialog by remember { mutableStateOf(false) }
    var selectedMonthSummary by remember { mutableStateOf<MonthSummary?>(null) }
    var inputSalaryText by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val bgResource = if (isDark) R.drawable.app_background_dark else R.drawable.app_background
    val textColor = if (isDark) Color.White else Color(0xFF2C3E50)


    // Главный контейнер-бокс для наложения объемного заднего фона
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = bgResource), // Твой размытый зелено-золотой фон
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Весь интерфейс идет поверх фона
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Главный заголовок экрана
            Text(
                text = "Мои деньги",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = textColor
            )

            // Верхняя карточка с текстурой шлифованного металла
            CurrentMonthCard(
                monthName = currentMonthName,
                totalDebt = finalCardBalance
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок раздела истории
            Text(
                text = "ИСТОРИЯ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = textColor
            )

            // Подзаголовки таблицы (Месяц, Остаток, Зарплата) с идеальным выравниванием по правому краю
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Месяц", fontSize = 12.sp, color = textColor, modifier = Modifier.weight(1.3f))
                Spacer(modifier = Modifier.weight(0.5f)) // Невидимый отступ над иконкой статуса
                Text(text = "Остаток", fontSize = 12.sp, color = textColor, textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
                Text(text = "Зарплата", fontSize = 12.sp, color = textColor, textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
            }

            // Выводим объемную таблицу месяцев из базы данных
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(monthlyData) { summary ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Клик разрешен только для активных месяцев учета
                            .clickable(enabled = summary.isActive) {
                                selectedMonthSummary = summary
                                inputSalaryText = summary.totalSalary.toInt().toString()
                                showDialog = true
                            }
                    ) {
                        val hasSurplus = summary.surplus > 0
                        val displayDebt = if (hasSurplus) 0.0 else summary.debt

                        MonthRowItem(
                            monthName = summary.monthName,
                            subTitle = if (hasSurplus) "${summary.year} (Выплачено: $${String.format("%,.0f", summary.totalPaid)})" else summary.year.toString(),
                            debt = displayDebt,
                            salary = summary.totalSalary,
                            isCurrentMonth = summary.isCurrentMonth,
                            isActive = summary.isActive
                        )
                    }
                }
            }
        }
    }

    // --- ДИАЛОГОВОЕ ОКНО ИЗМЕНЕНИЯ ЗАРПЛАТЫ ---
    if (showDialog && selectedMonthSummary != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Зарплата за ${selectedMonthSummary!!.monthName}") },
            text = {
                Column {
                    Text(text = "Укажите плановый размер зарплаты на этот месяц:", fontSize = 14.sp, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputSalaryText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) inputSalaryText = newValue
                        },
                        label = { Text("Сумма ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newSalary = inputSalaryText.toDoubleOrNull() ?: 0.0
                        if (newSalary > 0) {
                            viewModel.updateSalaryForMonth(selectedMonthSummary!!.dbKey, newSalary)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7A64))
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена", color = textColor)
                }
            }
        )
    }
}