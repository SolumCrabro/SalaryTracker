package com.example.salarytracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

// карточка "Текуций месяц" на главном экране.

@Composable
fun CurrentMonthCard(
    monthName: String,
    totalDebt: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp), // Сильно округлые углы как на эскизе
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE5E5E5) // Светло-серый цвет фона карточки
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "ТЕКУЩИЙ МЕСЯЦ: ${monthName.uppercase()}",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ОБЩИЙ ДОЛГ:",
                color = Color(0xFFC78165), // Терракотовый/коричневый цвет текста
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "₽${String.format("%,.0f", totalDebt)}",
                color = Color(0xFFC78165),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}