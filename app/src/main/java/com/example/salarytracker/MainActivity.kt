package com.example.salarytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val items = listOf(Screen.Home, Screen.Add, Screen.List)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

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
    ) { innerPadding ->
        // Здесь происходит переключение экранов

        val salaryViewModel: SalaryViewModel = viewModel()

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel = salaryViewModel) }  // Сюда подключим чуть позже
            composable(Screen.Add.route) {  AddScreen(viewModel = salaryViewModel) } // Передали ViewModel }
            composable(Screen.List.route)  { ListScreen(viewModel = salaryViewModel) }  // Сюда подключим в следующем шаге
        }
    }
}