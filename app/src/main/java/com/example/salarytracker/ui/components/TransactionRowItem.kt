package com.example.salarytracker.ui.components

import com.example.salarytracker.ui.theme.accentLight
import com.example.salarytracker.ui.theme.accentDark
import com.example.salarytracker.ui.theme.appAccentColor
import com.example.salarytracker.ui.theme.money
import com.example.salarytracker.data.AppCurrency
import com.example.salarytracker.data.formatMoney
import com.example.salarytracker.data.formatRate
import androidx.compose.foundation.Image
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
    val isDark = isAppInDarkTheme()

    val isSide = transaction.isSideIncome
    val isCard = transaction.paymentType == "CARD"

    val titleText = if (isSide && !transaction.sourceNote.isNullOrEmpty()) {
        "Доход: ${transaction.sourceNote}"
    } else {
        if (isCard) "Поступление на карту" else "Поступление наличными"
    }

    val iconResource = if (isCard) R.drawable.ic_card else R.drawable.ic_cash
    val itemTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)

    // Специальные цвета для стороннего дохода ("левый приход")
    val badgeBgColor = if (isSide) {
        if (isDark) accentDark.copy(alpha = 0.18f) else accentLight.copy(alpha = 0.15f)
    } else {
        Color(0xFF438A6E).copy(alpha = 0.15f)
    }

    val amountTextColor = if (isSide) {
        appAccentColor()
    } else {
        if (isDark) Color(0xFF438A6E) else Color(0xFF4A7A64)
    }

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = badgeBgColor),
            modifier = Modifier.size(40.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isSide) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Подработка",
                        tint = amountTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = titleText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = itemTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transaction.date.format(dateFormatter),
                fontSize = 13.sp,
                color = Color(0xFF7A7D84)
            )
            // Внесено через конвертер: "500 BYN по 2.95 · НБРБ"
            val originalAmount = transaction.originalAmount
            if (originalAmount != null && transaction.originalCurrency != null) {
                val rateInfo = transaction.exchangeRate?.let { " по ${formatRate(it)}" } ?: ""
                val sourceInfo = transaction.rateSource?.let { " · $it" } ?: ""
                Text(
                    text = formatMoney(originalAmount, AppCurrency.fromName(transaction.originalCurrency)) + rateInfo + sourceInfo,
                    fontSize = 12.sp,
                    color = Color(0xFF7A7D84)
                )
            }
        }

        Text(
            text = "+ ${money(transaction.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = amountTextColor
        )
    }
}
