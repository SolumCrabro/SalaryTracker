package com.example.salarytracker.ui.screens

import com.example.salarytracker.ui.theme.accentDark
import com.example.salarytracker.ui.theme.appAccentColor
import com.example.salarytracker.ui.theme.appCurrency
import com.example.salarytracker.ui.theme.money
import com.example.salarytracker.data.formatMoney
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.components.ProjectCard
import com.example.salarytracker.ui.components.ProjectCardColors
import com.example.salarytracker.ui.components.ProjectPaymentDialog
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.time.Month
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: SalaryViewModel = viewModel()) {
    val monthlyData by viewModel.monthlySummaries.collectAsState()
    val allActiveSummaries by viewModel.allActiveSummaries.collectAsState()
    val pendingProjects by viewModel.projectProgress.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    val isDark = isAppInDarkTheme()
    val chartCurrency = appCurrency()
    val textMeasurer = rememberTextMeasurer()

    // Состояния для всплывающего диалога добавления проекта
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectTitleText by remember { mutableStateOf("") }
    var projectAmountText by remember { mutableStateOf("") }

    // Диалог оплаты проекта (часть / полностью) и раскрытые карточки проектов
    var payProjectId by remember { mutableStateOf<Int?>(null) }
    var expandedProjectIds by remember { mutableStateOf(setOf<Int>()) }

    // Состояние для запуска анимации появления карточек
    var isVisible by remember { mutableStateOf(false) }

    // Аниматор прогресса для кольца и графика (от 0f до 1f)
    var animationTrigger by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        isVisible = true
        animationTrigger = 1f // Запускаем анимацию графиков
    }

    // Плавное изменение кодового прогресса за 1000 миллисекунд
    val chartProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 1000)
    )

    // 1. ДЛЯ ГРАФИКА: берем последние N месяцев (согласно настройке глубины истории)
    val activeMonthsChronological = monthlyData.filter { it.isActive }.reversed()

    // 2. ДЛЯ ОБЩЕЙ СВОДКИ ("ФИНАНСОВЫЙ ПУЛЬС"): считаем честно ЗА ВСЕ МЕСЯЦЫ с момента старта учета!
    val activeHistory = allActiveSummaries.filter { it.isActive }
    val totalSalarySum = activeHistory.sumOf { it.totalSalary }
    val totalDebtSum = activeHistory.sumOf { it.debt }

    // Все выплаты по окладу и подработкам за всё время с момента регистрации
    val mainIncomeSum = allTransactions.filter { !it.isSideIncome }.sumOf { it.amount }
    val sideIncomeSum = allTransactions.filter { it.isSideIncome }.sumOf { it.amount }

    // Честный прогресс закрытия плановой зарплаты за все время
    val salaryProgress = if (totalSalarySum > 0) {
        (mainIncomeSum / totalSalarySum).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60)

    val emeraldColor = if (isDark) Color(0xFF438A6E) else Color(0xFF4A7A64)
    val goldColor = appAccentColor()

    val borderStroke = androidx.compose.foundation.BorderStroke(
        1.5.dp,
        if (isDark) Color.White.copy(alpha = 0.1f) else accentDark
    )

    val projectColors = ProjectCardColors(
        card = cardBgColor,
        border = borderStroke,
        mainText = mainTextColor,
        label = labelTextColor,
        gold = goldColor,
        emerald = emeraldColor,
        track = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    )

    Box(modifier = Modifier.fillMaxSize().background(mainBgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Аналитика баланса",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = mainTextColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // БЛОК 1: Финансовый пульс (Понятная сводка в цифрах + честный прогресс)
            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500))) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ФИНАНСОВЫЙ ПУЛЬС",
                            fontSize = 11.sp,
                            color = labelTextColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Четыре карточки метрик 2х2
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("План зарплаты", fontSize = 11.sp, color = labelTextColor)
                                Text(
                                    text = money(totalSalarySum),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = mainTextColor
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Выплачено (основное)", fontSize = 11.sp, color = labelTextColor)
                                Text(
                                    text = money(mainIncomeSum),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Остаток долга", fontSize = 11.sp, color = labelTextColor)
                                Text(
                                    text = money(totalDebtSum),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDebtSum > 0) goldColor else emeraldColor
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Доп. доходы (проекты)", fontSize = 11.sp, color = labelTextColor)
                                Text(
                                    text = money(sideIncomeSum),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Честная шкала прогресса
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "План закрыт на:", fontSize = 12.sp, color = labelTextColor)
                            Text(
                                text = "${(salaryProgress * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { salaryProgress * chartProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = emeraldColor,
                            trackColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // БЛОК 2: Сдвоенный столбчатый график сравнения месяцев "План / Факт"
            Text(text = "Сравнение по месяцам", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = mainTextColor)
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500, delayMillis = 150))) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "СООТНОШЕНИЕ ПЛАН / ВЫПЛАЧЕНО",
                                fontSize = 11.sp,
                                color = labelTextColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            // Компактная легенда
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isDark) Color(0xFF333538) else Color(0xFFD2D4D8), RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("План", fontSize = 10.sp, color = labelTextColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(emeraldColor, RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Факт", fontSize = 10.sp, color = labelTextColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            val pointsCount = activeMonthsChronological.size
                            if (pointsCount > 0) {
                                val paddingX = 40.dp.toPx()
                                val chartWidth = size.width - (paddingX * 2)
                                val widthInterval = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth

                                val maxVal = activeMonthsChronological.maxOf { maxOf(it.totalSalary, it.totalPaid) }.coerceAtLeast(100.0)
                                val paddingTop = 20.dp.toPx()
                                val paddingBottom = 25.dp.toPx()
                                val chartHeight = size.height - paddingTop - paddingBottom

                                val barWidth = 12.dp.toPx()
                                val barSpacing = 2.dp.toPx()

                                // Горизонтальные линии сетки
                                for (i in 0..2) {
                                    val y = paddingTop + chartHeight * (i / 2f)
                                    drawLine(
                                        color = mainTextColor.copy(alpha = 0.05f),
                                        start = Offset(paddingX - 12.dp.toPx(), y),
                                        end = androidx.compose.ui.geometry.Offset(size.width - paddingX + 12.dp.toPx(), y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                activeMonthsChronological.forEachIndexed { index, summary ->
                                    val xCenter = paddingX + (index * widthInterval)

                                    val planHeight = (summary.totalSalary / maxVal * chartHeight).toFloat() * chartProgress
                                    val paidHeight = (summary.totalPaid / maxVal * chartHeight).toFloat() * chartProgress

                                    // 1. Столбец "План" (серо-пыльный)
                                    val planLeft = xCenter - barWidth - barSpacing
                                    val planTop = paddingTop + chartHeight - planHeight
                                    drawRoundRect(
                                        color = if (isDark) Color(0xFF333538) else Color(0xFFD2D4D8),
                                        topLeft = androidx.compose.ui.geometry.Offset(planLeft, planTop),
                                        size = Size(barWidth, planHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    // 2. Столбец "Факт" (изумрудный, либо золотой при наличии долга)
                                    val paidLeft = xCenter + barSpacing
                                    val paidTop = paddingTop + chartHeight - paidHeight
                                    val barColor = if (summary.debt > 0) goldColor else emeraldColor
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = androidx.compose.ui.geometry.Offset(paidLeft, paidTop),
                                        size = androidx.compose.ui.geometry.Size(barWidth, paidHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    // Название месяца снизу
                                    val shortMonthName = if (summary.monthName.length > 3) summary.monthName.take(3) else summary.monthName
                                    val monthTextLayout = textMeasurer.measure(
                                        text = shortMonthName,
                                        style = TextStyle(color = labelTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    )
                                    drawText(
                                        textLayoutResult = monthTextLayout,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            x = xCenter - (monthTextLayout.size.width / 2f),
                                            y = size.height - monthTextLayout.size.height
                                        )
                                    )

                                    // Значения над столбцами в виде компактной дроби "Факт/План"
                                    val valTextLayout = textMeasurer.measure(
                                        text = "${formatMoney(summary.totalPaid, chartCurrency)}/${formatMoney(summary.totalSalary, chartCurrency)}",
                                        style = TextStyle(color = mainTextColor.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                    val topY = minOf(planTop, paidTop) - valTextLayout.size.height - 4.dp.toPx()
                                    drawText(
                                        textLayoutResult = valTextLayout,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            x = xCenter - (valTextLayout.size.width / 2f),
                                            y = topY
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // БЛОК 3: "Проекты в работе" (Заметки / Подработки)
            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500, delayMillis = 250))) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Проекты в работе",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = mainTextColor
                        )

                        IconButton(
                            onClick = { showAddProjectDialog = true },
                            modifier = Modifier
                                .background(goldColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить проект",
                                tint = goldColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pendingProjects.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor)
                        ) {
                            Text(
                                text = "Нет проектов в работе.\nНажмите +, чтобы добавить подработку или замещаемый заказ.",
                                fontSize = 14.sp,
                                color = labelTextColor,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pendingProjects.forEach { progress ->
                                val projectId = progress.project.id
                                ProjectCard(
                                    progress = progress,
                                    expanded = projectId in expandedProjectIds,
                                    colors = projectColors,
                                    onToggle = {
                                        expandedProjectIds = if (projectId in expandedProjectIds) {
                                            expandedProjectIds - projectId
                                        } else {
                                            expandedProjectIds + projectId
                                        }
                                    },
                                    onPayClick = { payProjectId = projectId },
                                    // Проект с оплатами висит, пока не закрыт полностью
                                    onDelete = if (progress.hasPayments) null else {
                                        { viewModel.deletePendingProject(progress.project) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ДИАЛОГ 1: Добавление нового проекта в работу ---
    if (showAddProjectDialog) {
        val dialogBg = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF)
        val dialogTitle = if (isDark) Color.White else Color(0xFF111214)

        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            containerColor = dialogBg,
            titleContentColor = dialogTitle,
            title = { Text(text = "Новый проект", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = projectTitleText,
                        onValueChange = { projectTitleText = it },
                        label = { Text("Название задачи") },
                        placeholder = { Text("Например, Проект шкафа") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldColor,
                            focusedLabelColor = goldColor,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                            unfocusedLabelColor = labelTextColor,
                            focusedTextColor = dialogTitle,
                            unfocusedTextColor = dialogTitle
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = projectAmountText,
                        onValueChange = { val s = it.replace(',', '.'); if (s.all { c -> c.isDigit() || c == '.' }) projectAmountText = s },
                        label = { Text("Ожидаемая сумма (${appCurrency().label})") },
                        placeholder = { Text("100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldColor,
                            focusedLabelColor = goldColor,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                            unfocusedLabelColor = labelTextColor,
                            focusedTextColor = dialogTitle,
                            unfocusedTextColor = dialogTitle
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = projectAmountText.toDoubleOrNull() ?: 0.0
                        if (projectTitleText.isNotBlank() && amount > 0) {
                            viewModel.addPendingProject(projectTitleText.trim(), amount)
                            projectTitleText = ""
                            projectAmountText = ""
                            showAddProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = goldColor)
                ) {
                    Text("Добавить", color = if (isDark) Color(0xFF111214) else Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProjectDialog = false }) {
                    Text("Отмена", color = goldColor)
                }
            }
        )
    }

    // --- ДИАЛОГ 2: Оплата проекта — внести часть или закрыть полностью ---
    pendingProjects.find { it.project.id == payProjectId }?.let { progress ->
        ProjectPaymentDialog(
            progress = progress,
            isDark = isDark,
            colors = projectColors,
            onPartial = { amount, paymentType ->
                viewModel.addProjectPayment(progress.project, amount, paymentType)
                expandedProjectIds = expandedProjectIds + progress.project.id
                payProjectId = null
            },
            onCloseFully = { paymentType ->
                viewModel.completeProject(progress.project, paymentType)
                payProjectId = null
            },
            onDismiss = { payProjectId = null }
        )
    }
}
