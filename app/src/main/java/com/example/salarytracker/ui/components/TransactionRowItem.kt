package com.example.salarytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.R
import com.example.salarytracker.data.Transaction
import java.time.format.DateTimeFormatter

@Composable
fun TransactionRowItem(transaction: Transaction) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    // ЛОГИКА ВЫБОРА ИКОНКИ: Подставляем твои загруженные SVG из drawable
    val isCard = transaction.paymentType == "CARD"
    val iconResource = if (isCard) R.drawable.ic_card else R.drawable.ic_cash
    val paymentTypeText = if (isCard) "Поступление на карту" else "Поступление наличными"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая подложка для твоей иконки
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Отрисовываем твою кастомную SVG иконку
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = paymentTypeText,
                        modifier = Modifier.size(24.dp) // Размер самой иконки внутри кружка
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paymentTypeText, // Динамический текст
                    fontSize = 15.sp,
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

            // Сумма теперь выводится со знаком $
            Text(
                text = "+ $${String.format("%,.0f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A7A64)
            )
        }
    }
}
