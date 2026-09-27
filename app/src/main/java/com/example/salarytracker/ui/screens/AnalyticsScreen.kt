package com.example.salarytracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.salarytracker.data.PendingProject
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: SalaryViewModel = viewModel()) {
    val monthlyData by viewModel.monthlySummaries.collectAsState()
    val pendingProjects by viewModel.allPendingProjects.collectAsState()

    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()

    // Состояния для всплывающего диалога добавления проекта
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectTitleText by remember { mutableStateOf("") }
    var projectAmountText by remember { mutableStateOf("") }

    // Состояния для диалога выбора способа оплаты (Карта/Наличные) при клике на галочку
    var projectToComplete by remember { mutableStateOf<PendingProject?>(null) }
    var showCompleteDialog by remember { mutableStateOf(false) }

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

    val activeMonthsChronological = monthlyData.filter { it.isActive }.reversed()
    val totalSalarySum = activeMonthsChronological.sumOf { it.totalSalary }
    val totalDebtSum = activeMonthsChronological.sumOf { it.debt }
    val totalPaidSum = activeMonthsChronological.sumOf { it.totalPaid }

    val debtPercentage = if (totalSalarySum > 0) (totalDebtSum / totalSalarySum).toFloat() else 0f
    val paidPercentage = if (totalSalarySum > 0) (totalPaidSum / totalSalarySum).toFloat() else 0f

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60)

    val emeraldColor = if (isDark) Color(0xFF438A6E) else Color(0xFF4A7A64)
    val goldColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

    val borderStroke = androidx.compose.foundation.BorderStroke(
        1.5.dp,
        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE5B067)
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

            // БЛОК 1: Карточка с анимированным кольцом
            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500))) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val strokeWidth = 14.dp.toPx()
                            drawCircle(color = Color.Gray.copy(alpha = 0.15f), style = Stroke(strokeWidth))

                            // Умножаем угол sweepAngle на анимированный прогресс chartProgress для эффекта закручивания!
                            drawArc(
                                color = emeraldColor,
                                startAngle = -90f,
                                sweepAngle = (paidPercentage * 360f) * chartProgress,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = goldColor,
                                startAngle = -90f + ((paidPercentage * 360f) * chartProgress),
                                sweepAngle = (debtPercentage * 360f) * chartProgress,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column {
                            Text(text = "СТАТУС БАЛАНСА", fontSize = 12.sp, color = labelTextColor, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(emeraldColor, RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Закрыто: ${(paidPercentage * 100).toInt()}%", fontSize = 14.sp, color = mainTextColor)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(goldColor, RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Долг: ${(debtPercentage * 100).toInt()}%", fontSize = 14.sp, color = mainTextColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // БЛОК 2: Карточка с плавно прорисовывающимся линейным графиком
            Text(text = "Динамика остатков", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = mainTextColor)
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500, delayMillis = 150))) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "ИЗМЕНЕНИЕ ДОЛГА ПО МЕСЯЦАМ", fontSize = 11.sp, color = labelTextColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))

                        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            val pointsCount = activeMonthsChronological.size
                            if (pointsCount > 0) {
                                val paddingX = 30.dp.toPx()
                                val chartWidth = size.width - (paddingX * 2)

                                val widthInterval = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth
                                val maxDebt = activeMonthsChronological.maxOf { it.debt }.coerceAtLeast(1.0)

                                val paddingTop = 25.dp.toPx()
                                val paddingBottom = 25.dp.toPx()
                                val chartHeight = size.height - paddingTop - paddingBottom

                                // Сетка графика
                                for (i in 0..2) {
                                    val y = paddingTop + chartHeight * (i / 2f)
                                    drawLine(
                                        color = mainTextColor.copy(alpha = 0.06f),
                                        start = androidx.compose.ui.geometry.Offset(paddingX, y),
                                        end = androidx.compose.ui.geometry.Offset(size.width - paddingX, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Базовые координаты точек
                                val coordinates = activeMonthsChronological.mapIndexed { index, summary ->
                                    val x = paddingX + (index * widthInterval)
                                    val y =
                                        paddingTop + chartHeight - (summary.debt / maxDebt * chartHeight).toFloat()
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }
// ЭФФЕКТ АНИМАЦИИ ГРАФИКА: Линия прорисовывается за счет ограничения по текущему прогрессу chartProgress
                                if (pointsCount > 1) {
                                    for (i in 0 until coordinates.size - 1) {
                                        val startPt = coordinates[i]
                                        val endPt = coordinates[i + 1]
// Вычисляем, до куда линия успела добежать на текущем кадре
                                        val currentEndX = startPt.x + (endPt.x - startPt.x) * chartProgress
                                        val currentEndY = startPt.y + (endPt.y - startPt.y) * chartProgress
// Рисуем отрезок, только если до него дошел прогресс
                                        if (chartProgress > (i.toFloat() / (pointsCount - 1))) {
                                            drawLine(
                                                color = goldColor,
                                                start = startPt,
                                                end = androidx.compose.ui.geometry.Offset(
                                                    currentEndX.coerceAtMost(endPt.x),
                                                    if (chartProgress >= ((i + 1).toFloat() / (pointsCount - 1))) endPt.y else currentEndY
                                                ),
                                                strokeWidth = 3.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
// 4. Появление кружков и текстовых меток подстраиваем под альфа-прогресс анимации
                                activeMonthsChronological.forEachIndexed { index, summary ->
                                    val pt = coordinates[index]
// Узлы и текст появляются плавно, когда линия добегает до них
                                    val triggerThreshold = index.toFloat() / pointsCount
                                    val ptAlpha = if (chartProgress >= triggerThreshold) (chartProgress - triggerThreshold) * pointsCount else 0f
                                    val cleanAlpha = ptAlpha.coerceIn(0f, 1f)
                                    drawCircle(color = cardBgColor.copy(alpha = cleanAlpha), radius = 6.dp.toPx(), center = pt)
                                    drawCircle(color = goldColor.copy(alpha = cleanAlpha), radius = 4.dp.toPx(), center = pt)
                                    val textStyleAmount = TextStyle(
                                        color = (if (summary.debt > 0) goldColor else emeraldColor).copy(alpha = cleanAlpha),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val textStyleMonth = TextStyle(
                                        color = labelTextColor.copy(alpha = cleanAlpha),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val amountTextLayout = textMeasurer.measure(
                                        text = if (summary.surplus > 0) "Переплата" else "$${summary.debt.toInt()}",
                                        style = textStyleAmount
                                    )
                                    drawText(
                                        textLayoutResult = amountTextLayout,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            x = pt.x - (amountTextLayout.size.width / 2f),
                                            y = pt.y - amountTextLayout.size.height - 6.dp.toPx()
                                        ),
                                        alpha = cleanAlpha
                                    )
                                    val shortMonthName = if (summary.monthName.length > 3) summary.monthName.take(3) else summary.monthName
                                    val monthTextLayout = textMeasurer.measure(
                                        text = shortMonthName,
                                        style = textStyleMonth
                                    )
                                    drawText(
                                        textLayoutResult = monthTextLayout,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            x = pt.x - (monthTextLayout.size.width / 2f),
                                            y = size.height - monthTextLayout.size.height
                                        ),
                                        alpha = cleanAlpha
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
                            pendingProjects.forEach { project ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(borderStroke, RoundedCornerShape(18.dp)),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = project.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = mainTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Добавлено: ${project.date.dayOfMonth} ${
                                                    project.date.month.getDisplayName(
                                                        java.time.format.TextStyle.SHORT,
                                                        Locale("ru")
                                                    )
                                                }",
                                                fontSize = 12.sp,
                                                color = labelTextColor
                                            )
                                        }

                                        Text(
                                            text = "$${String.format(Locale.US, "%,.0f", project.amount)}",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldColor
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Зеленая галочка "Завершить и зачислить"
                                        IconButton(
                                            onClick = {
                                                projectToComplete = project
                                                showCompleteDialog = true
                                            },
                                            modifier = Modifier
                                                .background(emeraldColor, RoundedCornerShape(50))
                                                .size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Оплачено",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Кнопка быстрого удаления проекта
                                        IconButton(
                                            onClick = { viewModel.deletePendingProject(project) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить проект",
                                                tint = labelTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
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
                        label = { Text("Ожидаемая сумма ($)") },
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

    // --- ДИАЛОГ 2: Подтверждение оплаты (Выбор: На карту / Наличные) ---
    if (showCompleteDialog && projectToComplete != null) {
        val dialogBg = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF)
        val dialogTitle = if (isDark) Color.White else Color(0xFF111214)
        var selectedPaymentType by remember { mutableStateOf("CARD") }

        AlertDialog(
            onDismissRequest = {
                showCompleteDialog = false
                projectToComplete = null
            },
            containerColor = dialogBg,
            titleContentColor = dialogTitle,
            title = { Text(text = "Зачисление оплаты", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Куда поступила оплата за «${projectToComplete!!.title}» ($${projectToComplete!!.amount.toInt()})?",
                        fontSize = 14.sp,
                        color = labelTextColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPaymentType == "CARD",
                            onClick = { selectedPaymentType = "CARD" },
                            colors = RadioButtonDefaults.colors(selectedColor = goldColor)
                        )
                        Text(text = "На карту", color = dialogTitle, fontSize = 15.sp)

                        Spacer(modifier = Modifier.width(20.dp))

                        RadioButton(
                            selected = selectedPaymentType == "CASH",
                            onClick = { selectedPaymentType = "CASH" },
                            colors = RadioButtonDefaults.colors(selectedColor = goldColor)
                        )
                        Text(text = "Наличными", color = dialogTitle, fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeProject(projectToComplete!!, selectedPaymentType)
                        showCompleteDialog = false
                        projectToComplete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldColor)
                ) {
                    Text("Зачислить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    projectToComplete = null
                }) {
                    Text("Отмена", color = goldColor)
                }
            }
        )
    }
}
