package com.example.salarytracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.R

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
    val alpha = if (isActive) 1f else 0.5f

    // Настройка обводки плашки
    val borderStroke = when {
        !isActive -> BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        isCurrentMonth && isDark -> {
            BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF9ECE9A), Color(0xFF6B9E78))))
        }
        isCurrentMonth && !isDark -> {
            BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFE5C07B), Color(0xFFB38F4F))))
        }
        else -> BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f))
    }

    val rowTexture = if (isDark) R.drawable.metal_bg_dark else R.drawable.metal_bg

    // Логика выбора иконки: если есть долг — крестик, если долга нет — галочка
    val hasDebt = debt > 0
    val iconImage = if (hasDebt) Icons.Default.Close else Icons.Default.CheckCircle

    // Цвета иконок: в темной теме — золото, в светлой — цветные
    val iconColor = when {
        !isActive -> Color(0xFF99A3A4)
        isDark -> Color(0xFFE5C07B) // Благородное золото для темной темы
        hasDebt -> Color(0xFFBD7C5D) // Терракотовый крестик для светлой темы
        isCurrentMonth -> Color(0xFFD4AF37) // Золотая галочка для текущего месяца
        else -> Color(0xFF4A7A64) // Зеленая галочка для прошлых месяцев
    }

    // Цвета текстов
    val titleColor = if (isDark) Color.White else Color(0xFF2C3E50)
    val salaryColor = if (isDark) Color(0xFF76A28E) else Color(0xFF4A7A64)
    val debtColor = Color(0xFFBD7C5D)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = rowTexture),
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Месяц и год
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(text = monthName, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = titleColor)
                    if (subTitle.isNotEmpty()) {
                        Text(text = subTitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // 2. ВОЗВРАЩЕННАЯ ИКОНКА СТАТУСА
                Icon(
                    imageVector = iconImage,
                    contentDescription = "Статус",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp).padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 3. Вертикальный разделитель
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.Gray.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 4. Цифры остатка и зарплаты
                if (isActive) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                        Text(text = "Остаток", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "$${String.format("%,.0f", debt)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = debtColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.0f)) {
                        Text(text = "Зарплата", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "$${String.format("%,.0f", salary)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = salaryColor
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(2.0f), contentAlignment = Alignment.CenterEnd) {
                        Text(text = "Нет истории (учет не велся)", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}
