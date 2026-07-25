package com.example.salarytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MonthRowItem(
    monthName: String,
    subTitle: String = "",
    debt: Double,
    salary: Double,
    isCurrentMonth: Boolean = false,
    isActive: Boolean = true
) {
    val alpha = if (isActive) 1f else 0.4f

    // Градиенты для обводок кодом: золотой для текущего, прозрачно-белый для остальных
    val borderGradient = if (isCurrentMonth && isActive) {
        Brush.linearGradient(listOf(Color(0xFFE5B067).copy(alpha = 0.5f), Color(0xFF9E7743).copy(alpha = 0.1f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.12f), Color(0xFFFFFFFF).copy(alpha = 0.02f)))
    }

    // Иконки из нового макета: тонкие Check и Close вместо залитых кружков
    val hasDebt = debt > 0
    val iconImage = if (hasDebt) Icons.Default.Close else Icons.Default.Check
    val iconColor = if (!isActive) Color(0xFF7A7D84) else (if (hasDebt) Color(0xFFC78165) else Color(0xFF438A6E))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, borderGradient),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2022).copy(alpha = 0.85f) // Матовое темное стекло кодом
        )
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Месяц и год
            Column(modifier = Modifier.weight(1.3f)) {
                Text(text = monthName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (subTitle.isNotEmpty()) {
                    Text(text = subTitle, fontSize = 12.sp, color = Color(0xFF7A7D84))
                }
            }

            // Тонкая аккуратная иконка статуса без подложки, как на макете
            Icon(
                imageVector = iconImage,
                contentDescription = "Статус",
                tint = iconColor,
                modifier = Modifier.size(24.dp).padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
            Spacer(modifier = Modifier.width(12.dp))

            if (isActive) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Остаток", fontSize = 11.sp, color = Color(0xFF7A7D84))
                    Text(
                        text = "$${String.format("%,.0f", debt)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5B067) // Золото для остатка
                    )
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Зарплата", fontSize = 11.sp, color = Color(0xFF7A7D84))
                    Text(
                        text = "$${String.format("%,.0f", salary)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF438A6E) // Изумруд для зарплаты
                    )
                }
            } else {
                Box(modifier = Modifier.weight(2.0f), contentAlignment = Alignment.CenterEnd) {
                    Text(text = "Нет истории (учет не велся)", fontSize = 13.sp, color = Color(0xFF7A7D84), textAlign = TextAlign.End)
                }
            }
        }
    }
}