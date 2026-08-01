package com.example.salarytracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


// Главный фон приложения (глубокий бархатный черный)
val AppDarkBackground = Color(0xFF111214)

// Матовое темное стекло для карточек (полупрозрачный графит)
val DarkGlassColor = Color(0xFF1E2022).copy(alpha = 0.85f)

// Цвета текстов и акцентов из макета
val GoldAccent = Color(0xFFE5B067)       // Насыщенное золото для долга
val EmeraldAccent = Color(0xFF438A6E)    // Изумрудный для выплаченной зарплаты
val GrayText = Color(0xFF7A7D84)         // Серый для подписей годов и заголовков

// Тонкие благородные градиенты для золотых обводок карточек
val GoldBorderGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE5B067).copy(alpha = 0.6f), Color(0xFF9E7743).copy(alpha = 0.2f))
)
val NormalBorderGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF).copy(alpha = 0.15f), Color(0xFFFFFFFF).copy(alpha = 0.03f))
)


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
