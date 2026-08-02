package com.example.salarytracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(viewModel: SalaryViewModel = viewModel()) {
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPaymentType by remember { mutableStateOf("CARD") }

    val isDark = isSystemInDarkTheme()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val context = LocalContext.current

    // Состояния для нового, стильного Material 3 календаря кодом
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    var showDatePickerState by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val borderStroke = if (isDark) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.03f)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE5B067), Color(0xFFE5B067)))
    }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60)
    val buttonColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

    val row1Templates = listOf(100, 200, 300)
    val row2Templates = listOf(400, 500, 600)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Text(
                    text = "Внесение средств",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                    color = mainTextColor
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(550)) +
                        slideInVertically(animationSpec = tween(550), initialOffsetY = { it / 4 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, borderStroke, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
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
                                focusedBorderColor = buttonColor,
                                focusedLabelColor = buttonColor,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                                unfocusedLabelColor = labelTextColor,
                                focusedTextColor = mainTextColor,
                                unfocusedTextColor = mainTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row1Templates.forEach { amount ->
                                    val isSelected = amountText == amount.toString()
                                    val animatedBorderColor by animateColorAsState(if (isSelected) buttonColor else Color.Gray.copy(alpha = 0.4f))
                                    val animatedTextColor by animateColorAsState(if (isSelected) buttonColor else labelTextColor)
                                    val animatedBgColor by animateColorAsState(if (isSelected) buttonColor.copy(alpha = 0.12f) else Color.Transparent)

                                    OutlinedButton(
                                        onClick = { amountText = amount.toString() },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, animatedBorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = animatedBgColor),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text(text = "+$$amount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = animatedTextColor)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row2Templates.forEach { amount ->
                                    val isSelected = amountText == amount.toString()
                                    val animatedBorderColor by animateColorAsState(if (isSelected) buttonColor else Color.Gray.copy(alpha = 0.4f))
                                    val animatedTextColor by animateColorAsState(if (isSelected) buttonColor else labelTextColor)
                                    val animatedBgColor by animateColorAsState(if (isSelected) buttonColor.copy(alpha = 0.12f) else Color.Transparent)

                                    OutlinedButton(
                                        onClick = { amountText = amount.toString() },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, animatedBorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = animatedBgColor),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text(text = "+$$amount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = animatedTextColor)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // Поле выбора даты
                        OutlinedTextField(
                            value = selectedDate.format(dateFormatter),
                            onValueChange = {},
                            label = { Text("Дата выплаты") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Выбрать дату",
                                    tint = buttonColor,
                                    modifier = Modifier.clickable { showDatePickerState = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePickerState = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = buttonColor,
                                focusedLabelColor = buttonColor,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                                unfocusedLabelColor = labelTextColor,
                                focusedTextColor = mainTextColor,
                                unfocusedTextColor = mainTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Куда поступили средства?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelTextColor
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
                                    colors = RadioButtonDefaults.colors(selectedColor = buttonColor)
                                )
                                Text(text = "На карту", fontSize = 15.sp, color = mainTextColor)
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            Row(
                                modifier = Modifier.clickable { selectedPaymentType = "CASH" },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentType == "CASH",
                                    onClick = { selectedPaymentType = "CASH" },
                                    colors = RadioButtonDefaults.colors(selectedColor = buttonColor)
                                )
                                Text(text = "Наличные", fontSize = 15.sp, color = mainTextColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Главная кнопка отправки данных
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
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
                        ) {
                            Text("Сохранить", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF111214) else Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- СТИЛЬНЫЙ АДАПТИВНЫЙ КАЛЕНДАРЬ MATERIAL 3 КОДОМ ---
    if (showDatePickerState) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerState = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePickerState = false
                    }
                ) {
                    Text("OK", color = buttonColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerState = false }) {
                    Text("ОТМЕНА", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = cardBgColor
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = cardBgColor,
                    titleContentColor = mainTextColor,
                    headlineContentColor = mainTextColor,
                    weekdayContentColor = labelTextColor,
                    subheadContentColor = labelTextColor,
                    selectedDayContainerColor = buttonColor,
                    selectedDayContentColor = if (isDark) Color(0xFF111214) else Color.White,
                    todayContentColor = buttonColor,
                    todayDateBorderColor = buttonColor
                )
            )
        }
    }
}
