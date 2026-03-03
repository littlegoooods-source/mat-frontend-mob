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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.data.model.ProductionListItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProductionsScreen(viewModel: ProductionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statuses = listOf("", "Completed", "Cancelled")
    val statusLabels = listOf("Все", "Завершённые", "Отменённые")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
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

        FloatingActionButton(
            onClick = viewModel::openCreateDialog,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Произвести")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)
        )
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
                val isAvailable = avail.canProduce || avail.isAvailable
                val allMaterials = avail.materials.ifEmpty { avail.missingMaterials }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAvailable) SuccessBg else ErrorBg
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (isAvailable) "Материалы доступны" else "Недостаточно материалов",
                            color = if (isAvailable) Success else Error,
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (allMaterials.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            allMaterials.forEach { m ->
                                val reqQty = if (m.requiredQuantity > 0) m.requiredQuantity else m.required
                                val availQty = if (m.availableQuantity > 0) m.availableQuantity else m.available
                                val matAvailable = m.isAvailable || availQty >= reqQty
                                Text(
                                    "${m.materialName}: ${fmtQty(reqQty)} / ${fmtQty(availQty)} ${m.materialUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (matAvailable) Success else Error
                                )
                            }
                        }
                        if (avail.estimatedCostPerUnit > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Себестоимость единицы: ${fmtCur(avail.estimatedCostPerUnit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                "Общая себестоимость: ${fmtCur(avail.estimatedTotalCost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
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
                    enabled = !uiState.isSaving && (uiState.availability?.let { it.canProduce || it.isAvailable } ?: false)
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
    val isCancelled = production.isCancelled || production.status.equals("Cancelled", ignoreCase = true)
    val statusColor = when {
        isCancelled -> Error
        production.status.equals("Completed", ignoreCase = true) -> Success
        else -> Primary
    }
    val statusText = when {
        isCancelled -> "Отменено"
        production.status.equals("Completed", ignoreCase = true) -> "Завершено"
        else -> production.status
    }
    val displayDate = formatIsoDate(
        production.productionDate.ifEmpty { production.createdAt }
    )
    val totalCost = if (production.totalCost > 0) production.totalCost else production.totalMaterialCost

    Surface(shape = RoundedCornerShape(12.dp), color = DarkCard) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        production.productName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (production.batchNumber.isNotEmpty()) {
                        Text(
                            "Партия: ${production.batchNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                StatusBadge(text = statusText, color = statusColor, bgColor = statusColor.copy(alpha = 0.15f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn("Дата", displayDate)
                InfoColumn("Кол-во", "${production.quantity} шт")
                InfoColumn("Себест.", if (production.costPerUnit > 0) fmtCur(production.costPerUnit) else "—")
                InfoColumn("Сумма", if (totalCost > 0) fmtCur(totalCost) else "—")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "На складе: ${production.inStockCount} шт",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row {
                    if (production.status.equals("Completed", ignoreCase = true) && !isCancelled) {
                        IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Cancel, null, tint = Warning, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

private fun fmtCur(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 0
    return f.format(value)
}

private fun fmtQty(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value)
}

private fun formatIsoDate(isoString: String): String {
    if (isoString.isBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = parser.parse(isoString.take(19)) ?: return isoString.take(10)
        SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU")).format(date)
    } catch (_: Exception) {
        isoString.take(10)
    }
}
