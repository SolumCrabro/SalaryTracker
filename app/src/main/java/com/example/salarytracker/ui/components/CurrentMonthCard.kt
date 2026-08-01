package com.example.salarytracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CurrentMonthCard(
    monthName: String,
    totalDebt: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isSurplus = totalDebt < 0
    val displayAmount = if (isSurplus) -totalDebt else totalDebt

    // ИСПРАВЛЕНИЕ: Меняем длинную надпись на простое слово «ПЕРЕПЛАТА:»
    val labelText = if (isSurplus) "ПЕРЕПЛАТА:" else "ОБЩИЙ ДОЛГ:"

    val borderStroke = if (isDark) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.03f)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE5B067), Color(0xFFE5B067)))
    }

    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)

    val textAmountColor = if (isSurplus) {
        if (isDark) Color(0xFF438A6E) else Color(0xFF4A7A64)
    } else {
        if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)
    }

    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF4E5156)

    // Форматируем число
    val formattedAmount = String.format(java.util.Locale.US, "%,.0f", displayAmount)

    // ИСПРАВЛЕНИЕ: Убрали знак минус перед суммой переплаты, теперь пишется просто чистая сумма
    val textToShow = "\$$formattedAmount"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.5.dp, borderStroke, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text(
                text = "ТЕКУЩИЙ МЕСЯЦ: ${monthName.uppercase()}",
                color = labelTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = labelText,
                color = labelTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = textToShow,
                color = textAmountColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
