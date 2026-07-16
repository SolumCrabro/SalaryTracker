package com.example.salarytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalaryTrackerTheme() {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val salaryViewModel: SalaryViewModel = viewModel()

    // Наблюдаем за состоянием первого запуска
    val isFirstRun by salaryViewModel.isFirstRun.collectAsState()
    val items = listOf(Screen.Home, Screen.Add, Screen.List)

    // Пока DataStore грузит значение из памяти (isFirstRun == null) — показываем пустой экран загрузки
    if (isFirstRun == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4A7A64))
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Скрываем нижнее меню, если пользователь находится на экране приветствия
    val showBottomBar = currentRoute != Screen.Welcome.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            // Если первый запуск — стартуем с Welcome, иначе — с Home
            startDestination = if (isFirstRun == true) Screen.Welcome.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onFinished = {
                    // После завершения перенаправляем на Главную и очищаем стек
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