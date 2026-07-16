package com.example.salarytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    isActive: Boolean = true // Новое поле: активен ли месяц для учета
) {
    // Неактивный месяц плавно приглушаем (прозрачность 50%) и убираем золотую рамку
    val alpha = if (isActive) 1f else 0.5f
    val border = if (isCurrentMonth && isActive) BorderStroke(2.dp, Color(0xFFD4AF37)) else null
    val iconColor = if (isCurrentMonth && isActive) Color(0xFFD4AF37) else Color(0xFF99A3A4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(alpha), // Применяем приглушение цвета
        shape = RoundedCornerShape(16.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = monthName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                if (subTitle.isNotEmpty()) {
                    Text(text = subTitle, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Статус",
                tint = iconColor,
                modifier = Modifier.size(28.dp).padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(1.dp).height(30.dp))
            Spacer(modifier = Modifier.width(16.dp))

            if (isActive) {
                // Если месяц активен — выводим стандартные расчеты
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    Text(text = "Остаток", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "₽${String.format("%,.0f", debt)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = if (debt > 0) Color(0xFFC78165) else Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    Text(text = "Зарплата", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "₽${String.format("%,.0f", salary)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFF4A7A64)
                    )
                }
            } else {
                // Если месяц ДО начала ведения учета — пишем "Нет истории" на всю правую часть
                Box(
                    modifier = Modifier.weight(2f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Нет истории (учет не велся)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}