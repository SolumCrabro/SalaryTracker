package com.example.salarytracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var activeDismissState by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }

    val itemBorderGradient = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111214))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF438A6E).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.6f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.2f, size.height * 0.6f)
            )
        }

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
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022).copy(alpha = 0.8f)),
                        modifier = Modifier.padding(24.dp).border(1.dp, itemBorderGradient, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = "Вы еще не вносили платежи.\nВсе ваши поступления будут отображаться здесь.",
                            fontSize = 16.sp,
                            color = Color.White,
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
                    items(transactions, key = { it.id }) { transaction ->

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    transactionToDelete = transaction
                                    val dismissState = null
                                    activeDismissState = dismissState
                                    showDeleteDialog = true
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        // Проверяем сдвиг по целевому значению анимации
                        val isCurrentSwiped = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

                        SwipeToDismissBox(
                            state = dismissState,
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
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .border(1.2.dp, itemBorderGradient, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022).copy(alpha = 0.85f))
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

    if (showDeleteDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                coroutineScope.launch { activeDismissState?.reset() }
                transactionToDelete = null
                activeDismissState = null
            },
            containerColor = Color(0xFF1E2022),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF7A7D84),
            title = { Text(text = "Удаление платежа", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить платеж на сумму $${String.format("%,.0f", transactionToDelete!!.amount)}?",
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
                        // Теперь .reset() плавно вернет карточку поверх коричневого фона!
                        coroutineScope.launch {
                            activeDismissState?.reset()
                            transactionToDelete = null
                            activeDismissState = null
                        }
                    }
                ) {
                    Text("Отмена", color = Color(0xFFE5B067))
                }
            }
        )
    }
}