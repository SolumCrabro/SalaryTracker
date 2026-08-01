package com.example.salarytracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable // Добавлен для контроля анимации строк
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Изменили на itemsIndexed для каскадного эффекта
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Добавлен для применения эффектов сдвига и альфы
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.data.Transaction
import com.example.salarytracker.ui.components.TransactionRowItem
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(viewModel: SalaryViewModel = viewModel()) {
    val transactions by viewModel.allTransactions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    // Состояния для управления диалогом удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var activeDismissState by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }

    val itemBorderStroke = if (isDark) {
        BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f))))
    } else {
        BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    }

    val mainBgColor = if (isDark) Color(0xFF111214) else Color(0xFFF3F4F6)
    val cardBgColor = if (isDark) Color(0xFF1E2022).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
    val mainTextColor = if (isDark) Color.White else Color(0xFF1A1C1E)
    val dialogButtonColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "История поступлений",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = mainTextColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = itemBorderStroke,
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Вы еще не вносили платежи.\nВсе ваши поступления будут отображаться здесь.",
                            fontSize = 16.sp,
                            color = mainTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Используем itemsIndexed вместо обычного items для анимации
                    itemsIndexed(transactions, key = { _, transaction -> transaction.id }) { index, transaction ->

                        // НЕЗАВИСИМЫЕ АНИМАТОРЫ СТРОКИ: Создаются заново для каждого элемента
                        val itemAlpha = remember { Animatable(0f) }
                        val itemOffsetY = remember { Animatable(40f) } // Изначально плашка утоплена вниз на 40dp

                        // Триггер запуска анимации «волны» при загрузке данных
                        LaunchedEffect(transactions) {
                            // Задержка вычисляется динамически по индексу: 1-я строчка вылетит на 60мс позже 0-й
                            delay(100L + (index * 60L))

                            coroutineScope.launch {
                                itemAlpha.animateTo(1f, animationSpec = tween(350))
                            }
                            coroutineScope.launch {
                                itemOffsetY.animateTo(0f, animationSpec = tween(350))
                            }
                        }

                        val currentDismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    transactionToDelete = transaction
                                    showDeleteDialog = true
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        if (transactionToDelete == transaction) {
                            activeDismissState = currentDismissState
                        }

                        val isCurrentSwiped = currentDismissState.targetValue == SwipeToDismissBoxValue.EndToStart

                        // Оборачиваем айтем в Box с модификатором graphicsLayer для применения плавной анимации
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = itemAlpha.value          // Связываем прозрачность с аниматором
                                    translationY = itemOffsetY.value // Связываем сдвиг по вертикали с аниматором
                                }
                        ) {
                            SwipeToDismissBox(
                                state = currentDismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        if (isCurrentSwiped) Color(0xFFC78165) else Color.Transparent
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .background(color, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (isCurrentSwiped) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить",
                                                tint = Color.White,
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                        }
                                    }
                                },
                                content = {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        border = itemBorderStroke,
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
                                    ) {
                                        TransactionRowItem(transaction = transaction)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                coroutineScope.launch { activeDismissState?.reset() }
                transactionToDelete = null
                activeDismissState = null
            },
            containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF),
            titleContentColor = mainTextColor,
            textContentColor = if (isDark) Color(0xFF7A7D84) else Color(0xFF555A60),
            title = { Text(text = "Удаление платежа", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить платеж на сумму$ ${String.format(java.util.Locale.US, "%,.0f", transactionToDelete!!.amount)}?",
                fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        showDeleteDialog = false
                        transactionToDelete = null
                        activeDismissState = null
                        Toast.makeText(context, "Платеж успешно удален", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC78165))
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch { activeDismissState?.reset() }
                        transactionToDelete = null
                        activeDismissState = null
                    }
                ) {
                    Text("Отмена", color = dialogButtonColor)
                }
            }
        )
    }
}
