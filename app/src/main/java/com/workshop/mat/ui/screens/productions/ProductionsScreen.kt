package com.workshop.mat.ui.screens.productions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.data.model.ProductionListItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductionsScreen(viewModel: ProductionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statuses = listOf("", "Completed", "Cancelled")
    val statusLabels = listOf("Все", "Завершённые", "Отменённые")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = viewModel::openCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Произвести")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppDropdown(
            value = statusLabels[statuses.indexOf(uiState.statusFilter).coerceAtLeast(0)],
            onValueChange = { label ->
                val idx = statusLabels.indexOf(label)
                viewModel.updateStatusFilter(statuses.getOrElse(idx) { "" })
            },
            label = "Статус",
            options = statusLabels
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.productions.isEmpty() -> EmptyState("Нет производств")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.productions, key = { it.id }) { production ->
                    ProductionItem(
                        production = production,
                        onCancel = { viewModel.showCancelConfirm(production) },
                        onDelete = { viewModel.showDeleteConfirm(production) }
                    )
                }
            }
        }
    }

    // Create dialog
    if (uiState.showCreateDialog) {
        FormDialog(title = "Новое производство", onDismiss = viewModel::closeCreateDialog) {
            AppDropdown(
                value = uiState.products.find { it.id.toString() == uiState.formProductId }?.name ?: "Выберите изделие",
                onValueChange = { name ->
                    val p = uiState.products.find { it.name == name }
                    if (p != null) viewModel.updateFormProductId(p.id.toString())
                },
                label = "Изделие",
                options = uiState.products.map { it.name }
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.formQuantity,
                onValueChange = viewModel::updateFormQuantity,
                label = "Количество",
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.formNotes,
                onValueChange = viewModel::updateFormNotes,
                label = "Примечание",
                singleLine = false
            )

            // Availability check
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.isCheckingAvailability) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Проверка доступности...", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            uiState.availability?.let { avail ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (avail.isAvailable) SuccessBg else ErrorBg
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (avail.isAvailable) "Материалы доступны" else "Недостаточно материалов",
                            color = if (avail.isAvailable) Success else Error,
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (!avail.isAvailable && avail.missingMaterials.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            avail.missingMaterials.forEach { m ->
                                Text(
                                    "${m.materialName}: нужно ${m.required}, есть ${m.available} ${m.materialUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::createProduction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !uiState.isSaving && (uiState.availability?.isAvailable ?: false)
                ) { Text("Произвести") }
                OutlinedButton(onClick = viewModel::closeCreateDialog, modifier = Modifier.weight(1f)) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        }
    }

    uiState.showCancelConfirm?.let { p ->
        ConfirmDialog(
            title = "Отменить производство",
            message = "Отменить производство \"${p.productName}\" (${p.quantity} шт)?",
            confirmText = "Отменить",
            onConfirm = viewModel::cancelProduction,
            onDismiss = viewModel::dismissCancelConfirm,
            isDestructive = true
        )
    }

    uiState.showDeleteConfirm?.let { p ->
        ConfirmDialog(
            title = "Удалить",
            message = "Удалить запись производства?",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteProduction,
            onDismiss = viewModel::dismissDeleteConfirm,
            isDestructive = true
        )
    }
}

@Composable
private fun ProductionItem(
    production: ProductionListItemDto,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (production.status.lowercase()) {
        "completed" -> Success
        "cancelled" -> Error
        else -> Primary
    }
    val statusText = when (production.status.lowercase()) {
        "completed" -> "Завершено"
        "cancelled" -> "Отменено"
        else -> production.status
    }

    Surface(shape = RoundedCornerShape(12.dp), color = DarkCard) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(production.productName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Количество: ${production.quantity} шт", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                StatusBadge(text = statusText, color = statusColor, bgColor = statusColor.copy(alpha = 0.15f))
            }
            Text(
                fmtCur(production.totalMaterialCost),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            if (production.status.lowercase() == "completed") {
                IconButton(onClick = onCancel) { Icon(Icons.Default.Cancel, null, tint = Warning, modifier = Modifier.size(20.dp)) }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(20.dp)) }
        }
    }
}

private fun fmtCur(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 0
    return f.format(value)
}
