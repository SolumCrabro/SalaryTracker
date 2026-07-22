package com.example.salarytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.R

@Composable
fun CurrentMonthCard(
    monthName: String,
    totalDebt: Double,
    modifier: Modifier = Modifier
) {
    val isSurplus = totalDebt < 0
    val displayAmount = if (isSurplus) -totalDebt else totalDebt
    val labelText = if (isSurplus) "ПРОФИЦИТ / ПЕРЕПЛАТА:" else "ОБЩИЙ ДОЛГ:"

    // Цвета текста как на макете (мягкий коричнево-золотой для долга)
    val valueColor = if (isSurplus) Color(0xFF4A7A64) else Color(0xFFBD7C5D)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // Мягкая белая обводка сверху создает эффект блика и объема
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) // Глубокая тень для объема
    ) {
        // Box позволяет наложить текст поверх картинки-текстуры
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.metal_bg), // Твоя текстура металла
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text(
                    text = "ТЕКУЩИЙ МЕСЯЦ: ${monthName.uppercase()}",
                    color = Color(0xFF555555),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = labelText,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$${String.format("%,.0f", displayAmount)}",
                    color = valueColor,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}