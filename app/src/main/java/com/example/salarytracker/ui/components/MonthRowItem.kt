package com.example.salarytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
    val alpha = if (isActive) 1f else 0.5f

    // Премиальная золотая обводка-градиент для текущего активного месяца, для остальных — мягкий белый блик
    val borderStroke = if (isCurrentMonth && isActive) {
        BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFE5C07B), Color(0xFFB38F4F))))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
    }

    // Объемный неоморфический градиент фона плашки (от чисто белого к благородному светло-серому)
    val cardGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFEAEAEA))
    )

    // Логика иконки: Если есть долг — крестик, если долга нет — галочка
    val hasDebt = debt > 0
    val iconImage = if (hasDebt) Icons.Default.Close else Icons.Default.CheckCircle

    // Цвета иконок: Мягкий терракотовый для долга, золото/зеленый для закрытых месяцев
    val iconColor = when {
        !isActive -> Color(0xFF99A3A4)
        hasDebt -> Color(0xFFBD7C5D)
        isCurrentMonth -> Color(0xFFD4AF37)
        else -> Color(0xFF4A7A64)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .background(cardGradient)
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть: Название месяца и год
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = monthName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                if (subTitle.isNotEmpty()) {
                    Text(text = subTitle, fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Динамическая иконка статуса (крестик или галочка)
            Icon(
                imageVector = iconImage,
                contentDescription = "Статус",
                tint = iconColor,
                modifier = Modifier.size(28.dp).padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Тонкий вертикальный разделитель с эффектом объема
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (isActive) {
                // Колонки цифр с валютой $
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Остаток", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$${String.format("%,.0f", debt)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = if (debt > 0) Color(0xFFBD7C5D) else Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                    Text(text = "Зарплата", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$${String.format("%,.0f", salary)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFF4A7A64)
                    )
                }
            } else {
                // Если учет в этом месяце не велся
                Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.CenterEnd) {
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