package com.example.salarytracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

@Composable
fun AddScreen(viewModel: SalaryViewModel = viewModel()) {
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPaymentType by remember { mutableStateOf("CARD") }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Градиент для золотой рамки карточки
    val goldBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE5B067).copy(alpha = 0.4f), Color(0xFF9E7743).copy(alpha = 0.05f))
    )

    // Главный контейнер с кодовым неоновым фоном
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111214)) // Глубокий черный
    ) {
        // Рисуем мягкое неоновое свечение кодом на фоне
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE5B067).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.4f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.8f, size.height * 0.4f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Внесение средств",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                color = Color.White
            )

            // Парящая карточка из матового темного стекла
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .border(1.2.dp, goldBorderGradient, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E2022).copy(alpha = 0.85f)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' }) amountText = newValue
                        },
                        label = { Text("Сумма поступления ($)") },
                        placeholder = { Text("Например, 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5B067),
                            focusedLabelColor = Color(0xFFE5B067),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            unfocusedLabelColor = Color(0xFF7A7D84),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = selectedDate.format(dateFormatter),
                        onValueChange = {},
                        label = { Text("Дата выплаты") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать дату",
                                tint = Color(0xFFE5B067),
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5B067),
                            focusedLabelColor = Color(0xFFE5B067),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            unfocusedLabelColor = Color(0xFF7A7D84),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Куда поступили средства?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A7D84)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable { selectedPaymentType = "CARD" },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentType == "CARD",
                                onClick = { selectedPaymentType = "CARD" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5B067))
                            )
                            Text(text = "На карту", fontSize = 15.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Row(
                            modifier = Modifier.clickable { selectedPaymentType = "CASH" },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentType == "CASH",
                                onClick = { selectedPaymentType = "CASH" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5B067))
                            )
                            Text(text = "Наличные", fontSize = 15.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Финальная кодовая золотая кнопка
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                viewModel.addTransaction(amount, selectedDate, selectedPaymentType)
                                amountText = ""
                                selectedDate = LocalDate.now()
                                Toast.makeText(context, "Данные успешно сохранены!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Пожалуйста, введите корректную сумму", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5B067)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) { Text("Сохранить",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111214)
                    )
                    }
                }
            }
        }
    }
}