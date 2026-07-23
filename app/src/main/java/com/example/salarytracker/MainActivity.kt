package com.example.salarytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.salarytracker.navigation.Screen
import com.example.salarytracker.ui.screens.AddScreen
import com.example.salarytracker.ui.screens.HomeScreen
import com.example.salarytracker.ui.screens.ListScreen
import com.example.salarytracker.ui.screens.WelcomeScreen
import com.example.salarytracker.ui.theme.SalaryTrackerTheme
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

// ГЛАВНАЯ ТОЧКА ВХОДА (Её отсутствие ломало приложение)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Запускаем наше красивое приложение
            SalaryTrackerTheme {
            MainAppScreen()
        }}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val salaryViewModel: SalaryViewModel = viewModel()
    val isFirstRun by salaryViewModel.isFirstRun.collectAsState()
    val items = listOf(Screen.Home, Screen.Add, Screen.List)

    val isDark = isSystemInDarkTheme()

    if (isFirstRun == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4A7A64))
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Welcome.route

    // Объемный градиент для фона меню (в тон плашек месяцев)
    val bottomBarGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE5E5E5))
    )

    // Премиальный золотой градиент для обводки активных элементов
    val goldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE5C07B), Color(0xFFB38F4F))
    )
    val barTexture = if (isDark) R.drawable.metal_bg_dark else R.drawable.metal_bg
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Оборачиваем меню в Surface с закруглением верхних углов и глубокой тенью
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        // Тонкая золотая полоса-градиент по всему верхнему краю меню для блеска
                        .border(
                            BorderStroke(1.5.dp, goldGradient),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    color = Color.Transparent
                ) {

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = barTexture), // Текстура на фоне меню
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = Color.Transparent, // Этого параметра абсолютно достаточно для прозрачности!
                            tonalElevation = 0.dp
                        ) {
                            items.forEach { screen ->
                                val isSelected = currentRoute == screen.route

                                // Логика цвета текста и иконки: в темной теме неактивные пункты делаем белее
                                val unselectedColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF888888)
                                val selectedColor = Color(0xFFE5C07B) // Золото для активного экрана

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) selectedColor else unselectedColor
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                            color = if (isSelected) selectedColor else unselectedColor
                                        )
                                    },
                                    selected = isSelected,
                                    colors = NavigationBarItemDefaults.colors( // Вот здесь класс называется верно!
                                        indicatorColor = Color(0xFFE5C07B).copy(alpha = 0.15f),
                                        // Дополнительно пропишем цвета для самого айтема, чтобы зафиксировать прозрачность
                                        selectedIconColor = selectedColor,
                                        unselectedIconColor = unselectedColor
                                    ),
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isFirstRun == true) Screen.Welcome.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }, viewModel = salaryViewModel)
            }
            composable(Screen.Home.route) { HomeScreen(viewModel = salaryViewModel) }
            composable(Screen.Add.route) { AddScreen(viewModel = salaryViewModel) }
            composable(Screen.List.route) { ListScreen(viewModel = salaryViewModel) }
        }
    }
}