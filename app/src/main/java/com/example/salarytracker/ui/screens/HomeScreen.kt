package com.example.salarytracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.components.CurrentMonthCard
import com.example.salarytracker.ui.components.MonthRowItem
import com.example.salarytracker.ui.viewmodel.MonthSummary
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

@Composable
fun HomeScreen(viewModel: SalaryViewModel = viewModel()) {
    val monthlyData by viewModel.monthlySummaries.collectAsState()

    val currentMonthInfo = monthlyData.find { it.isCurrentMonth }
    val currentMonthName = currentMonthInfo?.monthName ?: "Текущий"

    // Вычисляем накопительный баланс для верхней карточки
    val activeMonths = monthlyData.filter { it.isActive }
    val totalDebtValue = activeMonths.sumOf { it.debt }
    val currentMonthSurplus = currentMonthInfo?.surplus ?: 0.0

    val finalCardBalance = if (currentMonthSurplus > 0) -currentMonthSurplus else totalDebtValue

    var showDialog by remember { mutableStateOf(false) }
    var selectedMonthSummary by remember { mutableStateOf<MonthSummary?>(null) }
    var inputSalaryText by remember { mutableStateOf("") }

    // Главный Box с фоном, нарисованным ЧИСТО КОДОМ
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111214)) // Глубокий черный фон
    ) {
        // Рисуем цветное неоновое свечение за карточками
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE5B067).copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * 0.6f, size.height * 0.2f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.6f, size.height * 0.2f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFC78165).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.55f),
                    radius = size.width * 0.4f
                ),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.1f, size.height * 0.55f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF438A6E).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.5f),
                    radius = size.width * 0.4f
                ),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.9f, size.height * 0.5f)
            )
        }

        // Интерфейс поверх кодового неонового фона
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Мои деньги",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White
            )

            CurrentMonthCard(
                monthName = currentMonthName,
                totalDebt = finalCardBalance
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "История",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color.White
            )

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

            LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(monthlyData) { summary ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            subTitle = summary.year.toString(),
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

    // --- ОБНОВЛЕННЫЙ ПРЕМИАЛЬНЫЙ ТЕМНЫЙ ДИАЛОГ ИЗМЕНЕНИЯ ЗАРПЛАТЫ ---
    if (showDialog && selectedMonthSummary != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF1E2022), // Матовое темное стекло кодом
            titleContentColor = Color.White,
            textContentColor = Color(0xFF7A7D84),
            title = { Text(text = "Зарплата за ${selectedMonthSummary!!.monthName}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Укажите плановый размер зарплаты на этот месяц:", fontSize = 14.sp, color = Color(0xFF7A7D84))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputSalaryText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) inputSalaryText = newValue
                        },
                        label = { Text("Сумма ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5B067), // Золотая обводка при клике
                            focusedLabelColor = Color(0xFFE5B067),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            unfocusedLabelColor = Color(0xFF7A7D84),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5B067)) // Золотая кнопка
                ) {
                    Text("Сохранить", color = Color(0xFF111214)) // Темный текст на золоте
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена", color = Color(0xFFE5B067)) // Золотой текст отмены
                }
            }
        )
    }
}
