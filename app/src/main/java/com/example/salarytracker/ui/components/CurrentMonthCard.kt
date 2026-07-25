package com.example.salarytracker.ui.components

import androidx.compose.foundation.border
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
    val isSurplus = totalDebt < 0
    val displayAmount = if (isSurplus) -totalDebt else totalDebt
    val labelText = if (isSurplus) "ПРОФИЦИТ / ПЕРЕПЛАТА:" else "ОБЩИЙ ДОЛГ:"

    // Элегантный градиент для золотой рамки
    val goldBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE5B067).copy(alpha = 0.4f), Color(0xFF9E7743).copy(alpha = 0.05f))
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // Изящная золотая обводка кодом
            .border(1.2.dp, goldBorderGradient, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2022).copy(alpha = 0.85f) // Матовое темное стекло кодом
        )
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text(
                text = "ТЕКУЩИЙ МЕСЯЦ: ${monthName.uppercase()}",
                color = Color(0xFF7A7D84),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = labelText,
                color = Color(0xFF7A7D84),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${String.format("%,.0f", displayAmount)}",
                color = if (isSurplus) Color(0xFF438A6E) else Color(0xFFE5B067), // Зеленый или Золотой из макета
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}