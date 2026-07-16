package com.example.salarytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.components.TransactionRowItem
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

@Composable
fun ListScreen(viewModel: SalaryViewModel = viewModel()) {
    // Подписываемся на список транзакций из базы данных.
    // Благодаря collectAsState, Compose сам перерисует экран, как только в БД что-то изменится!
    val transactions by viewModel.allTransactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Заголовок экрана
        Text(
            text = "История поступлений",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Если в базе данных еще нет записей
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Вы еще не вносили платежи.\nВсе ваши поступления будут отображаться здесь.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Выводим список, если данные есть
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // items автоматически развернет каждую транзакцию в нужную строчку
                items(transactions) { transaction ->
                    TransactionRowItem(transaction = transaction)
                }
            }
        }
    }
}