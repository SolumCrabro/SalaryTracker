package com.example.salarytracker.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onFinished: () -> Unit, viewModel: SalaryViewModel = viewModel()) {
    var salaryText by remember { mutableStateOf("") }
    val now = LocalDate.now()
    val ruLocale = Locale("ru")

    // Списки месяцев для выбора старта (текущий и 3 предыдущих для выбора)
    val startMonthOptions = (0..3).map { now.minusMonths(it.toLong()) }
    var selectedStartDate by remember { mutableStateOf(startMonthOptions[0]) } // По умолчанию старт с текущего
    var expandedDropdown by remember { mutableStateOf(false) }

    // Глубина отображения истории
    var selectedDepth by remember { mutableStateOf(3) } // По умолчанию выводить 3 месяца

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // На случай мелких экранов
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Добро пожаловать!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Text(text = "Давай настроим параметры учета.", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        // ПУНКТ 1: Ввод базовой зарплаты
        OutlinedTextField(
            value = salaryText,
            onValueChange = { if (it.all { char -> char.isDigit() }) salaryText = it },
            label = { Text("Зарплата по умолчанию в месяц ($)") },
            placeholder = { Text("Например, 75000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ПУНКТ 2: Выбор конкретного месяца начала учета (Выпадающее меню)
        Text(text = "2. Месяц начала отсчета:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown }
        ) {
            OutlinedTextField(
                value = "${selectedStartDate.month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase() }} ${selectedStartDate.year}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                startMonthOptions.forEach { date ->
                    DropdownMenuItem(
                        text = { Text("${date.month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase() }} ${date.year}") },
                        onClick = {
                            selectedStartDate = date
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ПУНКТ 3: Сколько месяцев отображать в таблице
        Text(text = "3. Сколько месяцев выводить в таблицу?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))

        val depthOptions = listOf(
            1 to "Только текущий месяц",
            2 to "Текущий и 1 предыдущий (всего 2)",
            3 to "Текущий и 2 предыдущих (всего 3)"
        )

        depthOptions.forEach { (depth, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDepth = depth }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedDepth == depth,
                    onClick = { selectedDepth = depth },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4A7A64))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка сохранения
        Button(
            onClick = {
                val salary = salaryText.toDoubleOrNull() ?: 0.0
                if (salary > 0) {
                    viewModel.completeOnboarding(
                        salary = salary,
                        startMonth = selectedStartDate.monthValue,
                        startYear = selectedStartDate.year,
                        depth = selectedDepth
                    )
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7A64))
        ) {
            Text("Начать работу", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}