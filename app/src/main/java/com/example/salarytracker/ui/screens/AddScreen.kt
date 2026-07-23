package com.example.salarytracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.R
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun AddScreen(viewModel: SalaryViewModel = viewModel()) {
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPaymentType by remember { mutableStateOf("CARD") }

    val isDark = isSystemInDarkTheme() // Проверяем тему устройства
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

    // Адаптивные ресурсы и цвета
    val bgResource = if (isDark) R.drawable.app_background_dark else R.drawable.app_background
    val metalTexture = if (isDark) R.drawable.metal_bg_dark else R.drawable.metal_bg
    val mainTextColor = if (isDark) Color.White else Color(0xFF2C3E50)
    val cardLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = bgResource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                color = mainTextColor
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Подкладываем текстуру металла внутрь карточки формы
                    Image(
                        painter = painterResource(id = metalTexture),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Поверх текстуры на темной теме накладываем легкое затемнение, чтобы текст читался идеально
                    if (isDark) {
                        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.2f)).clip(RoundedCornerShape(24.dp)))
                    }

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
                                focusedBorderColor = Color(0xFFE5C07B), // Благородное золото при клике
                                focusedLabelColor = Color(0xFFE5C07B),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray,
                                unfocusedLabelColor = cardLabelColor,
                                focusedTextColor = mainTextColor,
                                unfocusedTextColor = mainTextColor
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
                                    tint = Color(0xFFE5C07B),
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE5C07B),
                                focusedLabelColor = Color(0xFFE5C07B),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray,
                                unfocusedLabelColor = cardLabelColor,
                                focusedTextColor = mainTextColor,
                                unfocusedTextColor = mainTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Куда поступили средства?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cardLabelColor
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
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5C07B))
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
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5C07B))
                                )
                                Text(text = "Наличные", fontSize = 15.sp, color = mainTextColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    viewModel.addTransaction(amount, selectedDate, selectedPaymentType)
                                    amountText = ""
                                    selectedDate = LocalDate.now()
                                    Toast.makeText(context,
                                        "Данные успешно сохранены!",
                                        Toast.LENGTH_SHORT).show()
                                }
                                else
                                {Toast.makeText(context,
                                    "Пожалуйста, введите корректную сумму",
                                    Toast.LENGTH_SHORT).show()}
                                      }
                            ,modifier = Modifier.fillMaxWidth()
                                .height(56.dp),shape = RoundedCornerShape(16.dp)
                            ,colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C07B)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation =
                                if (isDark) 0.dp else 4.dp))
                        {Text("Сохранить",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2222))
                        }
                    }
                }
            }
        }
    }
}