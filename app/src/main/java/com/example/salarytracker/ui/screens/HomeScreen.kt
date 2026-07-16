package com.example.salarytracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.ui.components.CurrentMonthCard
import com.example.salarytracker.ui.components.MonthRowItem
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

@Composable
fun HomeScreen(viewModel: SalaryViewModel = viewModel()) {
    // Подписываемся на наши расчеты по месяцам

    val monthlyData by viewModel.monthlySummaries.collectAsState()

    // Ищем текущий месяц для верхней карточки общего долга
    val currentMonthInfo = monthlyData.find { it.isCurrentMonth }
    val currentMonthDebt = currentMonthInfo?.debt ?: 0.0
    val currentMonthName = currentMonthInfo?.monthName ?: "Текущий"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Большой главный заголовок
        Text(
            text = "Мои деньги",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF2C3E50)
        )

        // Карточка теперь берет реальный долг текущего месяца!
        CurrentMonthCard(
            monthName = currentMonthName,
            totalDebt = currentMonthDebt
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок раздела истории
        Text(
            text = "ИСТОРИЯ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = Color(0xFF2C3E50)
        )

        // Подзаголовки таблицы (Месяц, Флажок, Остаток, Зарплата)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Месяц", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            Text(text = "Флажок", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.5f))
            Text(text = "Остаток", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            Text(text = "Зарплата", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        }

        // Выводим таблицу месяцев из базы данных
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(monthlyData) { summary ->
                MonthRowItem(
                    monthName = summary.monthName,
                    subTitle = summary.year.toString(), // Вместо английского названия выведем год для наглядности
                    debt = summary.debt,
                    salary = summary.totalSalary,
                    isCurrentMonth = summary.isCurrentMonth
                )
            }
        }
    }
}