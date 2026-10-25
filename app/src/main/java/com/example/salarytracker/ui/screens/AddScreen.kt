package com.example.salarytracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import com.example.salarytracker.data.AppCurrency
import com.example.salarytracker.data.Bank
import com.example.salarytracker.data.CurrencyConverter
import com.example.salarytracker.data.formatMoney
import com.example.salarytracker.data.formatRate
import com.example.salarytracker.ui.theme.appCurrency
import java.util.Locale
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

    val isDark = isAppInDarkTheme()
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

    // --- Конвертер: ввод во второй валюте (BYN при учёте в $ и наоборот) ---
    val currency = appCurrency()
    val otherCurrency = currency.other
    val rateBank by viewModel.rateBank.collectAsState()
    val rates by viewModel.cachedRates.collectAsState()
    val loadingRates by viewModel.loadingRates.collectAsState()
    var inputInOther by remember { mutableStateOf(false) }
    val rate = rates[rateBank]
    val inputCurrency = if (inputInOther) otherCurrency else currency
    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val convertedAmount = if (rate != null) CurrencyConverter.convert(enteredAmount, currency, rate) else 0.0
    val showRateError: (String) -> Unit = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

    // Курс подтягивается сам, если его нет или он старше 3 часов
    LaunchedEffect(inputInOther, rateBank) {
        if (inputInOther) {
            val cached = rates[rateBank]
            if (cached == null || System.currentTimeMillis() - cached.updatedAtMillis > 3 * 60 * 60 * 1000L) {
                viewModel.refreshRate(rateBank, showRateError)
            }
        }
    }

    val templates = if (inputCurrency == AppCurrency.BYN) listOf(100, 200, 500, 1000, 1500, 2000) else listOf(100, 200, 300, 400, 500, 600)
    val row1Templates = templates.take(3)
    val row2Templates = templates.drop(3)

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
                                val sanitized = newValue.replace(',', '.')
                                if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                                    amountText = sanitized
                                }
                            },
                            label = { Text("Сумма поступления") },
                            trailingIcon = {
                                // Переключатель валюты ввода: "BYN ▾ | $"
                                val otherColor by animateColorAsState(if (inputInOther) buttonColor else labelTextColor.copy(alpha = 0.6f))
                                val mainColor by animateColorAsState(if (!inputInOther) buttonColor else labelTextColor.copy(alpha = 0.6f))
                                Row(
                                    modifier = Modifier.padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.clickable { inputInOther = true },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = otherCurrency.label, color = otherColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Вносить в ${otherCurrency.code}",
                                            tint = otherColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(text = "|", color = labelTextColor.copy(alpha = 0.4f), fontSize = 16.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                    Text(
                                        text = currency.label,
                                        color = mainColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { inputInOther = false }
                                    )
                                }
                            },
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
                                        Text(text = "+${formatMoney(amount.toDouble(), inputCurrency)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = animatedTextColor)
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
                                        Text(text = "+${formatMoney(amount.toDouble(), inputCurrency)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = animatedTextColor)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // Виджет курса и итог в валюте учёта — только при вводе во второй валюте
                        AnimatedVisibility(
                            visible = inputInOther,
                            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            Column {
                                RateWidget(
                                    bank = rateBank,
                                    rate = rate?.let { CurrencyConverter.rateFor(currency, it) },
                                    rateKind = when {
                                        rateBank == Bank.NBRB -> "Официальный курс"
                                        currency == AppCurrency.USD -> "Продажа USD"
                                        else -> "Покупка USD"
                                    },
                                    updatedAtMillis = rate?.updatedAtMillis,
                                    isLoading = rateBank in loadingRates,
                                    isDark = isDark,
                                    accentColor = buttonColor,
                                    mainTextColor = mainTextColor,
                                    labelTextColor = labelTextColor,
                                    onRefresh = { viewModel.refreshRate(rateBank, showRateError) }
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Итого к зачислению (${currency.code}):",
                                        fontSize = 15.sp,
                                        color = labelTextColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (rate != null) formatMoney(convertedAmount, currency) else "—",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = buttonColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }

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
                                if (amount > 0 && inputInOther && rate == null) {
                                    Toast.makeText(context, "Нет курса ${rateBank.shortTitle}. Обновите курс", Toast.LENGTH_SHORT).show()
                                } else if (amount > 0) {
                                    if (inputInOther && rate != null) {
                                        viewModel.addTransaction(
                                            amount = CurrencyConverter.convert(amount, currency, rate),
                                            date = selectedDate,
                                            paymentType = selectedPaymentType,
                                            originalAmount = amount,
                                            originalCurrency = otherCurrency,
                                            exchangeRate = CurrencyConverter.rateFor(currency, rate),
                                            rateSource = rateBank.shortTitle
                                        )
                                    } else {
                                        viewModel.addTransaction(amount, selectedDate, selectedPaymentType)
                                    }
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
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
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

private val rateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("ru"))

// Виджет курса выбранного банка с кнопкой обновления
@Composable
private fun RateWidget(
    bank: Bank,
    rate: Double?,
    rateKind: String,
    updatedAtMillis: Long?,
    isLoading: Boolean,
    isDark: Boolean,
    accentColor: Color,
    mainTextColor: Color,
    labelTextColor: Color,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (rate != null) "Курс ${bank.shortTitle}: 1 USD = ${formatRate(rate)} BYN" else "Курс ${bank.shortTitle}: нет данных",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = mainTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (updatedAtMillis != null) {
                    val time = Instant.ofEpochMilli(updatedAtMillis).atZone(ZoneId.systemDefault()).format(rateTimeFormatter)
                    "$rateKind · Обновлено: $time"
                } else {
                    "Нажмите «обновить»"
                },
                fontSize = 12.sp,
                color = labelTextColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                .clickable(enabled = !isLoading, onClick = onRefresh),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = accentColor)
            } else {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Обновить курс", tint = accentColor)
            }
        }
    }
}
