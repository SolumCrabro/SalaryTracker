package com.example.salarytracker.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    val isDark = isSystemInDarkTheme()

    val startMonthOptions = (0..3).map { now.minusMonths(it.toLong()) }
    var selectedStartDate by remember { mutableStateOf(startMonthOptions[0]) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedDepth by remember { mutableStateOf(3) }

    val borderStroke = if (isDark) {
        BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.03f))))
    } else {
        BorderStroke(1.5.dp, Color(0xFFE5B067))
    }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60)
    val buttonColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Добро пожаловать!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = mainTextColor)
            Text(text = "Давай настроим параметры учета.", fontSize = 16.sp, color = labelTextColor, modifier = Modifier.padding(bottom = 24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(borderStroke, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    OutlinedTextField(
                        value = salaryText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) salaryText = it },
                        label = { Text("Зарплата по умолчанию в месяц ($)") },
                        placeholder = { Text("Например, 1500") },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "2. Месяц начала отсчета:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelTextColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = "${selectedStartDate.month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase() }} ${selectedStartDate.year}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = buttonColor,
                                focusedLabelColor = buttonColor,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                                unfocusedLabelColor = labelTextColor,
                                focusedTextColor = mainTextColor,
                                unfocusedTextColor = mainTextColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(cardBgColor)
                        ) {
                            startMonthOptions.forEach { date ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${date.month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase() }} ${date.year}",
                                            color = mainTextColor
                                        )
                                    },
                                    onClick = {
                                        selectedStartDate = date
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "3. Сколько месяцев выводить в таблицу?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelTextColor)
                    Spacer(modifier = Modifier.height(8.dp))

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
                                colors = RadioButtonDefaults.colors(selectedColor = buttonColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 15.sp, color = mainTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
                    ) {Text(text = "Начать работу", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if
                            (isDark) Color(0xFF111214) else Color.White)
                    }
                }
            }
        }
    }
}