package com.example.salarytracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Палитра для ТЁМНОЙ ТЕМЫ (из твоего нового рендера)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5C07B),          // Золотой акцент для меню и иконок
    background = Color(0xFF1E2222),       // Глубокий темно-графитовый фон
    surface = Color(0xFF2D3232),          // Цвет плашек месяцев (темный металл)
    onBackground = Color(0xFFFFFFFF),     // Белый текст на темном фоне
    onSurface = Color(0xFFEEEEEE),        // Светло-серый текст на плашках
    secondary = Color(0xFF76A28E)         // Мягкий зеленый для зарплат и профицита
)

// 2. Палитра для СВЕТЛОЙ ТЕМЫ (то, что мы использовали ранее)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB38F4F),          // Наше стандартное золото
    background = Color(0xFFF5F5F5),       // Светлый фон
    surface = Color(0xFFFFFFFF),          // Белые карточки
    onBackground = Color(0xFF2C3E50),     // Темный текст
    onSurface = Color(0xFF2C3E50),
    secondary = Color(0xFF4A7A64)         // Стандартный зеленый
)

// Дополнительные кастомные неоновые цвета с макета
val NeonGreenGlow = Color(0xFF9ECE9A).copy(alpha = 0.4f) // Зеленое свечение контура
val DarkMetalText = Color(0xFFBD7C5D) // Терракотовый долг

@Composable
fun SalaryTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Авто-определение темы устройства!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Твои стандартные шрифты
        content = content
    )
}
