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

    val isCard = transaction.paymentType == "CARD"
    val iconResource = if (isCard) R.drawable.ic_card else R.drawable.ic_cash
    val paymentTypeText = if (isCard) "Поступление на карту" else "Поступление наличными"

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Круглая изумрудная подложка для SVG
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF438A6E).copy(alpha = 0.15f)),
            modifier = Modifier.size(40.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconResource),
                    contentDescription = paymentTypeText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = paymentTypeText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White // Белый текст
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transaction.date.format(dateFormatter),
                fontSize = 13.sp,
                color = Color(0xFF7A7D84) // Серый текст из макета
            )
        }

        // Сумма в благородном изумрудном цвете выплат
        Text(
            text = "+ $${String.format("%,.0f", transaction.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF438A6E)
        )
    }
}