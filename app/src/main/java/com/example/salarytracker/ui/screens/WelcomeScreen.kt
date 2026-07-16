package com.example.salarytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun WelcomeScreen(onFinished: () -> Unit, viewModel: SalaryViewModel = viewModel()) {
    var salaryText by remember { mutableStateOf("") }
    var selectedOffset by remember { mutableStateOf(2) } // По умолчанию текущий + 2 предыдущих месяца (всего 3)

    val now = LocalDate.now()
    val ruLocale = Locale("ru")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Добро пожаловать!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Text(
            text = "Давай настроим приложение под тебя.",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 1. Поле ввода зарплаты по умолчанию
        OutlinedTextField(
            value = salaryText,
            onValueChange = { if (it.all { char -> char.isDigit() }) salaryText = it },
            label = { Text("Твоя стандартная зарплата в месяц (₽)") },
            placeholder = { Text("Например, 75000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Выбор стартового месяца учета
        Text(
            text = "С какого месяца вести учет задолженностей?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        val options = listOf(
            0 to "Только текущий (${now.month.getDisplayName(TextStyle.FULL, ruLocale)})",
            2 to "Текущий и 2 предыдущих",
            3 to "Текущий и 3 предыдущих"
        )

        options.forEach { (offset, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedOffset = offset }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOffset == offset,
                    onClick = { selectedOffset = offset },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4A7A64))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = description, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 3. Кнопка сохранения
        Button(
            onClick = {
                val salary = salaryText.toDoubleOrNull() ?: 0.0
                if (salary > 0) {
                    viewModel.completeOnboarding(salary, selectedOffset)
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