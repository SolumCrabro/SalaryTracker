package com.example.salarytracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salarytracker.data.BackupData
import com.example.salarytracker.ui.theme.isAppInDarkTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

// Подтверждение восстановления: показывает, что лежит в файле, до замены текущих данных
@Composable
fun BackupRestoreDialog(
    backup: BackupData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E2022) else Color(0xFFFFFFFF)
    val dialogTitle = if (isDark) Color.White else Color(0xFF111214)
    val accentColor = if (isDark) Color(0xFFE5B067) else Color(0xFFBD7C5D)
    val dateText = backup.createdAt.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        titleContentColor = dialogTitle,
        textContentColor = Color(0xFF7A7D84),
        title = { Text(text = "Восстановить данные?", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = "Копия от $dateText:", fontSize = 14.sp, color = dialogTitle)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• платежей: ${backup.transactions.size}", fontSize = 14.sp)
                Text(text = "• месяцев с ручной зарплатой: ${backup.salaryConfigs.size}", fontSize = 14.sp)
                Text(text = "• проектов в работе: ${backup.projects.size}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Текущие платежи, оклады, проекты и настройки будут заменены данными из файла.",
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Восстановить", color = if (isDark) Color(0xFF111214) else Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = accentColor)
            }
        }
    )
}
