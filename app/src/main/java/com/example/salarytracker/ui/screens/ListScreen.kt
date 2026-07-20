package com.example.salarytracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.data.Transaction
import com.example.salarytracker.ui.components.TransactionRowItem
import com.example.salarytracker.ui.viewmodel.SalaryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(viewModel: SalaryViewModel = viewModel()) {
    val transactions by viewModel.allTransactions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Состояния для управления диалогом удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Храним ссылку на стейт свайпа той карточки, которую сейчас планируем удалить
    var currentDismissState by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }

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
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Вы еще не вносили платежи.\nВсе ваши поступления будут отображаться здесь.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->

                    val dismissState = rememberSwipeToDismissBoxState()

                    // Безопасный перехват свайпа: отслеживаем целевое значение анимации (targetValue)
                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart && transactionToDelete == null && !showDeleteDialog) {
                        transactionToDelete = transaction
                        currentDismissState = dismissState
                        showDeleteDialog = true
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false, // Запрещаем свайп вправо
                        enableDismissFromEndToStart = true,  // Только влево
                        backgroundContent = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFC78165)
                                    else -> Color.Transparent
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .background(color, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = Color.White,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        },
                        content = {
                            TransactionRowItem(transaction = transaction)
                        }
                    )
                }
            }
        }
    }

    // --- ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ---
    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                coroutineScope.launch { currentDismissState?.reset() } // Плавный возврат карточки на место
                transactionToDelete = null
                currentDismissState = null
            },
            title = { Text(text = "Удаление платежа") },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить платеж на сумму ₽${String.format("%,.0f", transactionToDelete!!.amount)}?",
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        showDeleteDialog = false
                        transactionToDelete = null
                        currentDismissState = null
                        Toast.makeText(context, "Платеж успешно удален", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC78165))
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // Мягко возвращаем карточку назад на экран с помощью встроенного метода .reset()
                        coroutineScope.launch {
                            currentDismissState?.reset()
                            transactionToDelete = null
                            currentDismissState = null
                        }
                    }
                ) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}