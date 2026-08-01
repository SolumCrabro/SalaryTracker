package com.example.salarytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalaryTrackerTheme {
                val isDark = isSystemInDarkTheme()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
                ) {
                    MainAppScreen()
                }
            }
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

    val goldGradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFFE5B067).copy(alpha = 0.5f), Color(0xFF9E7743).copy(alpha = 0.15f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE5B067), Color(0xFFE5B067))) // Сплошное золото для дня
    }

    // Чистый белый фон меню для светлой темы
    val barBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.95f) else Color(0xFFFFFFFF)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isDark) 0.dp else 12.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .border(BorderStroke(1.5.dp, goldGradient), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    color = barBgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val contentColor = if (isSelected) {
                                if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)
                            } else {
                                Color(0xFF7A7D84)
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) contentColor.copy(alpha = 0.12f) else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = contentColor
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
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
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
