package com.example.salarytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
    val isDark = isSystemInDarkTheme()
    val alpha = if (isActive) 1f else 0.4f
    val hasDebt = debt > 0

    val borderStroke = when {
        !isActive -> BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        isDark -> BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f))))
        hasDebt -> BorderStroke(1.5.dp, Color(0xFFE5B067))
        else -> BorderStroke(1.5.dp, Color(0xFF4A7A64))
    }

    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)

    val iconImage = if (hasDebt) Icons.Default.Close else Icons.Default.Check
    val iconColor = when {
        !isActive -> Color(0xFF99A3A4)
        isDark -> Color(0xFFE5B067)
        hasDebt -> Color(0xFFBD7C5D)
        else -> Color(0xFF4A7A64)
    }

    val titleColor = if (isDark) Color.White else Color(0xFF1C1E21)
    val labelTextColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60)
    val salaryColor = if (isDark) Color(0xFF76A28E) else Color(0xFF4A7A64)
    val debtColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

    // Исправленное безопасное форматирование чисел кодом
    val formattedDebt = String.format(java.util.Locale.US, "%,.0f", debt)
    val formattedSalary = String.format(java.util.Locale.US, "%,.0f", salary)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(text = monthName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
                if (subTitle.isNotEmpty()) {
                    Text(text = subTitle, fontSize = 12.sp, color = labelTextColor)
                }
            }

            Icon(
                imageVector = iconImage,
                contentDescription = "Статус",
                tint = iconColor,
                modifier = Modifier.size(24.dp).padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            val lineAlpha = if (isDark) 0.08f else 0.15f
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(if (isDark) Color.White.copy(alpha = lineAlpha) else Color.Black.copy(alpha = lineAlpha)))
            Spacer(modifier = Modifier.width(12.dp))

            if (isActive) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Остаток", fontSize = 11.sp, color = labelTextColor)
                    Text(
                        text = "\$$formattedDebt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = debtColor
                    )
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Зарплата", fontSize = 11.sp, color = labelTextColor)
                    Text(
                        text = "\$$formattedSalary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = salaryColor
                    )
                }
            } else {
                Box(modifier = Modifier.weight(2.0f), contentAlignment = Alignment.CenterEnd) {
                    Text(text = "Нет истории (учет не велся)", fontSize = 13.sp, color = labelTextColor, textAlign = TextAlign.End)
                }
            }
        }
    }
}
