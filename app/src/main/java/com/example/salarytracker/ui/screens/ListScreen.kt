package com.example.salarytracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salarytracker.R
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
    val isDark = isSystemInDarkTheme()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var currentDismissState by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }

    val bgResource = if (isDark) R.drawable.app_background_dark else R.drawable.app_background
    val mainTextColor = if (isDark) Color.White else Color(0xFF2C3E50)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = bgResource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF2D3232).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.7f)
                        ),
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
                    items(transactions, key = { it.id }) { transaction ->

                        val dismissState = rememberSwipeToDismissBoxState()

                        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart && transactionToDelete == null && !showDeleteDialog) {
                            transactionToDelete = transaction
                            currentDismissState = dismissState
                            showDeleteDialog = true
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
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
                                        .background(color, RoundedCornerShape(20.dp)),
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
                                // Накладываем текстуру металла на каждую плашку внутри TransactionRowItem
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val rowTexture = if (isDark) R.drawable.metal_bg_dark else R.drawable.metal_bg
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Image(
                                                painter = painterResource(id = rowTexture),
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            TransactionRowItem(transaction = transaction)
                                        }
                                    }
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
                coroutineScope.launch { currentDismissState?.reset() }
                transactionToDelete = null
                currentDismissState = null
            },
            title = { Text(text = "Удаление платежа") },
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
                        coroutineScope.launch { currentDismissState?.reset() }
                        transactionToDelete = null
                        currentDismissState = null
                    }
                ) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}