package com.example.salarytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.salarytracker.data.Transaction
import java.time.format.DateTimeFormatter

@Composable
fun TransactionRowItem(transaction: Transaction) {
    // Форматируем дату в удобный вид, например "14 октября 2026"
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), // Очень светлый серый фон
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая или просто аккуратная иконка стрелочки вверх (символ поступления)
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Светло-зеленый фон для иконки
                modifier = Modifier.size(40.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Поступление",
                        tint = Color(0xFF4A7A64) // Наш зеленый цвет зарплаты
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Текстовый блок с датой
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Поступление средств",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.date.format(dateFormatter),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Сумма
            Text(
                text = "+ $${String.format("%,.0f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A7A64)
            )
        }
    }
}