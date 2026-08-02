package com.example.salarytracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Add : Screen("add", "Внесение", Icons.Default.AddCircle)
    object List : Screen("list", "Список", Icons.Default.List)

    object Welcome : Screen("welcome", "Регистрация", Icons.Default.Home)

    object Analytics : Screen("analytics", "Аналитика", Icons.Default.DateRange)
}
