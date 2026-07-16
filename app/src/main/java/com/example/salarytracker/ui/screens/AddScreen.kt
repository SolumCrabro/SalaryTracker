package com.example.salarytracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.widget.Toast

@Composable
fun AddScreen(viewModel: SalaryViewModel = viewModel()) { // Подключаем ViewModel по умолчанию
    // Состояние для хранения введенной суммы
    var amountText by remember { mutableStateOf("") }

    // Состояние для хранения выбранной даты (по умолчанию сегодня)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Форматтер для красивого отображения даты (например, "14.07.2026")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    // Контекст нужен для вызова системного диалога календаря
    val context = LocalContext.current

    // Логика настройки системного календаря Android
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Месяцы в Calendar начинаются с 0, поэтому прибавляем 1
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Большой заголовок экрана
        Text(
            text = "Внесение средств",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color(0xFF2C3E50)
        )

        // 1. Поле ввода суммы денег
        OutlinedTextField(
            value = amountText,
            onValueChange = { newValue ->
                // Разрешаем вводить только цифры (и одну точку для копеек, если нужно)
                if (newValue.all { it.isDigit() || it == '.' }) {
                    amountText = newValue
                }
            },
            label = { Text("Сумма поступления ($)") },
            placeholder = { Text("Например, 500") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Открывает цифровую клавиатуру
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Поле выбора даты (выглядит как текстовое поле, но при клике открывает календарь)
        OutlinedTextField(
            value = selectedDate.format(dateFormatter),
            onValueChange = {},
            label = { Text("Дата выплаты") },
            readOnly = true, // Запрещаем ручной ввод с клавиатуры
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Выбрать дату",
                    modifier = Modifier.clickable { datePickerDialog.show() }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() } // Открытие календаря по тапу на любое место поля
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Кнопка сохранения данных
        Button(
            onClick = {

                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    // Вызываем метод ViewModel для сохранения в базу данных!
                    viewModel.addTransaction(amount, selectedDate)

                    // Очищаем форму и сбрасываем дату на текущую
                    amountText = ""
                    selectedDate = LocalDate.now()

                    // Показываем пользователю всплывающее уведомление об успехе
                    Toast.makeText(context, "Данные успешно сохранены!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Пожалуйста, введите корректную сумму", Toast.LENGTH_SHORT).show()

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A7A64) // Зеленоватый оттенок, как у зарплаты на главном экране
            )
        ) {
            Text(
                text = "Сохранить",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}