package com.example.salarytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    // Если долг меньше нуля (или равен 0 при наличии излишков) — это профицит!
    val isSurplus = totalDebt < 0
    val displayAmount = if (isSurplus) -totalDebt else totalDebt

    // Меняем цвета в зависимости от состояния счета
    val labelText = if (isSurplus) "ПРОФИЦИТ / ПЕРЕПЛАТА:" else "ОБЩИЙ ДОЛГ:"
    val valueColor = if (isSurplus) Color(0xFF4A7A64) else Color(0xFFC78165) // Зеленый для профицита, терракотовый для долга

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text(
                text = "ТЕКУЩИЙ МЕСЯЦ: ${monthName.uppercase()}",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = labelText,
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Выводим знак ₽ вместо устаревшего $
            Text(
                text = "$${String.format("%,.0f", displayAmount)}",
                color = valueColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}