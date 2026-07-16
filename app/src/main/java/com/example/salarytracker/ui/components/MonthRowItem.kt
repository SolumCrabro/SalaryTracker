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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MonthRowItem(
    monthName: String,
    subTitle: String = "", // Для английских подписей под месяцем (August/July)
    debt: Double,
    salary: Double,
    isCurrentMonth: Boolean = false
) {
    // Если месяц текущий — делаем золотую обводку, иначе — без обводки
    val border = if (isCurrentMonth) BorderStroke(2.dp, Color(0xFFD4AF37)) else null
    // Цвет галочки: золотой для текущего, серый для закрытых месяцев
    val iconColor = if (isCurrentMonth) Color(0xFFD4AF37) else Color(0xFF99A3A4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
            // Название месяца
            Column(modifier = Modifier.weight(1f)) {
                Text(text = monthName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                if (subTitle.isNotEmpty()) {
                    Text(text = subTitle, fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Иконка Флажок (Галочка)
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Статус",
                tint = iconColor,
                modifier = Modifier.size(28.dp).padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Вертикальный разделитель
            Box(modifier = Modifier.width(1.dp).height(30.dp).padding(horizontal = 1.dp))

            Spacer(modifier = Modifier.width(16.dp))

            // Остаток (Долг)
            Column(
                horizontalAlignment = Alignment.End, // Выравнивает элементы колонки по правому краю
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Остаток", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = "$${String.format("%,.0f", debt)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = if (debt > 0) Color(0xFFC78165) else Color.Gray
                )
            }

// Зарплата
            Column(
                horizontalAlignment = Alignment.End, // Выравнивает элементы колонки по правому краю
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Зарплата", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = "$${String.format("%,.0f", salary)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = Color(0xFF4A7A64)
                )
            }
        }
    }
}