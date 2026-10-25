package com.example.salarytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.domain.ProjectProgress
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ruLocale = Locale("ru")

private fun money(value: Double): String = "$${String.format(Locale.US, "%,.0f", value)}"

private fun shortDate(date: LocalDate): String =
    "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, ruLocale).trimEnd('.')}"

private fun paymentTypeTitle(type: String): String = if (type == "CASH") "Наличные" else "Карта"

class ProjectCardColors(
    val card: Color,
    val border: BorderStroke,
    val mainText: Color,
    val label: Color,
    val gold: Color,
    val emerald: Color,
    val track: Color
)

// Тонкая золотая полоска прогресса оплаты проекта
@Composable
private fun ProjectProgressBar(fraction: Float, colors: ProjectCardColors, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(600), label = "projectProgress")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(colors.gold)
        )
    }
}

/*
Карточка проекта в работе. Свернута: название, сумма (или "выплачено / всего") и полоска прогресса.
Развернута (тап по карточке): прогресс, история платежей и остаток к выплате.
*/
@Composable
fun ProjectCard(
    progress: ProjectProgress,
    expanded: Boolean,
    colors: ProjectCardColors,
    onToggle: () -> Unit,
    onPayClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val project = progress.project

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(colors.border, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = project.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.mainText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Добавлено: ${shortDate(project.date)}", fontSize = 12.sp, color = colors.label)
                }

                Text(
                    text = if (progress.hasPayments) "${money(progress.paid)} / ${money(project.amount)}" else money(project.amount),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gold
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Зеленая галочка: внести часть или закрыть проект полностью
                IconButton(
                    onClick = onPayClick,
                    modifier = Modifier
                        .background(colors.emerald, RoundedCornerShape(50))
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Оплата",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Удалить можно только проект без оплат
                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить проект",
                            tint = colors.label,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (progress.hasPayments || expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                ProjectProgressBar(progress.fraction, colors)
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${money(progress.paid)} / ${money(project.amount)} выплачено",
                        fontSize = 14.sp,
                        color = colors.gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (progress.payments.isEmpty()) {
                        Text(text = "Оплат пока не было", fontSize = 14.sp, color = colors.label)
                    } else {
                        progress.payments.forEachIndexed { index, payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Платёж ${index + 1} (${shortDate(payment.date)}) · ${paymentTypeTitle(payment.paymentType)}",
                                    fontSize = 14.sp,
                                    color = colors.mainText,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = money(payment.amount), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.emerald)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.emerald,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Остаток к выплате",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.gold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = money(progress.remaining), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.gold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "В работе",
                            fontSize = 12.sp,
                            color = colors.gold,
                            modifier = Modifier
                                .background(colors.gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Быстрый диалог по галочке: внести часть суммы или закрыть проект полностью
@Composable
fun ProjectPaymentDialog(
    progress: ProjectProgress,
    isDark: Boolean,
    colors: ProjectCardColors,
    onPartial: (amount: Double, paymentType: String) -> Unit,
    onCloseFully: (paymentType: String) -> Unit,
    onDismiss: () -> Unit
) {
    val remaining = progress.remaining
    val initialAmount = if (remaining % 1.0 == 0.0) remaining.toLong().toString() else remaining.toString()
    var amountText by remember(progress.project.id) { mutableStateOf(initialAmount) }
    var paymentType by remember { mutableStateOf("CARD") }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val dialogTitle = if (isDark) Color.White else Color(0xFF111214)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF),
        titleContentColor = dialogTitle,
        title = { Text(text = "Оплата «${progress.project.title}»", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Выплачено ${money(progress.paid)} из ${money(progress.project.amount)}, остаток ${money(remaining)}",
                    fontSize = 14.sp,
                    color = colors.label
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { val s = it.replace(',', '.'); if (s.all { c -> c.isDigit() || c == '.' }) amountText = s },
                    label = { Text("Сумма платежа ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.gold,
                        focusedLabelColor = colors.gold,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.4f),
                        unfocusedLabelColor = colors.label,
                        focusedTextColor = dialogTitle,
                        unfocusedTextColor = dialogTitle
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentType == "CARD",
                        onClick = { paymentType = "CARD" },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                    )
                    Text(text = "На карту", color = dialogTitle, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = paymentType == "CASH",
                        onClick = { paymentType = "CASH" },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                    )
                    Text(text = "Наличными", color = dialogTitle, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPartial(amount, paymentType) },
                    enabled = amount > 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold)
                ) {
                    Text(
                        text = if (progress.isClosedBy(amount)) "Внести и закрыть проект" else "Внести часть",
                        color = if (isDark) Color(0xFF111214) else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onCloseFully(paymentType) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.emerald)
                ) {
                    Text(text = "Закрыть полностью (${money(remaining)})", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = colors.gold) }
        }
    )
}
