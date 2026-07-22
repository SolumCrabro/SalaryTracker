package com.example.salarytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.salarytracker.ui.viewmodel.SalaryViewModel

// ГЛАВНАЯ ТОЧКА ВХОДА (Её отсутствие ломало приложение)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Запускаем наше красивое приложение
            MainAppScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val salaryViewModel: SalaryViewModel = viewModel()
    val isFirstRun by salaryViewModel.isFirstRun.collectAsState()
    val items = listOf(Screen.Home, Screen.Add, Screen.List)

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
                    NavigationBar(
                        modifier = Modifier.background(bottomBarGradient),
                        containerColor = Color.Transparent, // Выключаем плоский цвет по умолчанию
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        // Золотая иконка для активного экрана, серая для остальных
                                        tint = if (isSelected) Color(0xFFB38F4F) else Color(0xFF888888)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        // Золотой текст для активной вкладки
                                        color = if (isSelected) Color(0xFFB38F4F) else Color(0xFF888888)
                                    )
                                },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    // Овал-индикатор вокруг иконки делаем мягким золотистым с прозрачностью
                                    indicatorColor = Color(0xFFE5C07B).copy(alpha = 0.25f)
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