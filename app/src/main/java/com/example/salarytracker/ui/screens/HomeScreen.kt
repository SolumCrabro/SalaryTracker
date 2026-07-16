package com.example.salarytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val currentMonthDebt = currentMonthInfo?.debt ?: 0.0
    val currentMonthName = currentMonthInfo?.monthName ?: "Текущий"

    // Состояния для управления диалоговым окном редактирования
    var showDialog by remember { mutableStateOf(false) }
    var selectedMonthSummary by remember { mutableStateOf<MonthSummary?>(null) }
    var inputSalaryText by remember { mutableStateOf("") }

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
            color = Color(0xFF2C3E50)
        )

        CurrentMonthCard(
            monthName = currentMonthName,
            totalDebt = currentMonthDebt
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ИСТОРИЯ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = Color(0xFF2C3E50)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Месяц", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(0.5f))
            Text(text = "Остаток", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(text = "Зарплата", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(monthlyData) { summary ->
                // Оборачиваем плашку в Box с clickable, чтобы отлавливать нажатия
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMonthSummary = summary
                            inputSalaryText = summary.totalSalary.toInt().toString()
                            showDialog = true
                        }
                ) {
                    MonthRowItem(
                        monthName = summary.monthName,
                        subTitle = summary.year.toString(),
                        debt = summary.debt,
                        salary = summary.totalSalary,
                        isCurrentMonth = summary.isCurrentMonth
                    )
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
                    Text(text = "Укажите плановый размер зарплаты на этот месяц:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputSalaryText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) inputSalaryText = newValue
                        },
                        label = { Text("Сумма (₽)") },
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
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}