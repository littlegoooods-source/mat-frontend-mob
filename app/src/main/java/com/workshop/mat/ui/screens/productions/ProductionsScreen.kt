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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProductionsScreen(viewModel: ProductionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statuses = listOf("", "Completed", "Cancelled")
    val statusLabels = listOf("Все", "Завершённые", "Отменённые")
    Box(modifier = Modifier.fillMaxSize()) {
        NotificationBanner(
            message = uiState.snackbarMessage,
            onDismiss = viewModel::clearSnackbar
        )
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

    }

    // Create dialog
    if (uiState.showCreateDialog) {
        FormDialog(
            title = "Новое производство",
            onDismiss = viewModel::closeCreateDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
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

    val fields = mutableListOf(
        CardField("ИЗДЕЛИЕ", production.productName),
        CardField("ДАТА", displayDate),
    )
    if (production.batchNumber.isNotEmpty()) {
        fields.add(CardField("ПАРТИЯ", production.batchNumber))
    }
    fields.add(CardField("КОЛ-ВО", "${production.quantity} шт"))
    fields.add(CardField("СТАТУС", statusText, valueColor = statusColor))
    if (production.costPerUnit > 0) {
        fields.add(CardField("СЕБЕСТОИМОСТЬ", fmtCur(production.costPerUnit)))
    }
    if (totalCost > 0) {
        fields.add(CardField("ОБЩАЯ СУММА", fmtCur(totalCost)))
    }
    fields.add(CardField("НА СКЛАДЕ", "${production.inStockCount} шт"))

    WebStyleCard(
        fields = fields,
        actions = {
            if (production.status.equals("Completed", ignoreCase = true) && !isCancelled) {
                OutlinedButton(
                    onClick = onCancel,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Warning)
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Отменить", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            OutlinedButton(
                onClick = onDelete,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Удалить", style = MaterialTheme.typography.labelMedium)
            }
        }
    )
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
