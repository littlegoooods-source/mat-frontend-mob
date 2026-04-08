package com.workshop.mat.ui.screens.finished

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
import com.workshop.mat.data.model.FinishedProductListItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinishedProductsScreen(viewModel: FinishedProductsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statuses = listOf("", "InStock", "Sold", "WrittenOff")
    val statusLabels = listOf("Все", "На складе", "Продано", "Списано")

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
                uiState.items.isEmpty() -> EmptyState("Нет готовой продукции")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items, key = { it.id }) { item ->
                        FinishedProductItem(
                            item = item,
                            onSell = { viewModel.openSellDialog(item) },
                            onWriteOff = { viewModel.openWriteOffDialog(item) },
                            onReturn = { viewModel.showReturnConfirm(item) },
                            onDelete = { viewModel.showDeleteConfirm(item) }
                        )
                    }
                }
            }
        }

    }

    // Sell dialog
    uiState.showSellDialog?.let { item ->
        val cost = item.costPerUnit.takeIf { it > 0 } ?: item.materialCost
        SmallDialog(
            title = "Продажа: ${item.productName}",
            onDismiss = viewModel::closeSellDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            Text("Себестоимость: ${fmtCur(cost)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = uiState.sellPrice,
                onValueChange = viewModel::updateSellPrice,
                label = "Цена продажи",
                keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::sellProduct,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    enabled = !uiState.isSaving
                ) { Text("Продать") }
                OutlinedButton(onClick = viewModel::closeSellDialog, modifier = Modifier.weight(1f)) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        }
    }

    // Write-off dialog
    uiState.showWriteOffDialog?.let { item ->
        SmallDialog(
            title = "Списание: ${item.productName}",
            onDismiss = viewModel::closeWriteOffDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            AppTextField(
                value = uiState.writeOffReason,
                onValueChange = viewModel::updateWriteOffReason,
                label = "Причина списания",
                singleLine = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::writeOffProduct,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Warning),
                    enabled = !uiState.isSaving
                ) { Text("Списать") }
                OutlinedButton(onClick = viewModel::closeWriteOffDialog, modifier = Modifier.weight(1f)) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        }
    }

    // Return confirm
    uiState.showReturnConfirm?.let { item ->
        ConfirmDialog(
            title = "Вернуть на склад",
            message = "Вернуть \"${item.productName}\" на склад?",
            confirmText = "Вернуть",
            onConfirm = viewModel::returnToStock,
            onDismiss = viewModel::dismissReturnConfirm
        )
    }

    // Delete confirm
    uiState.showDeleteConfirm?.let { item ->
        ConfirmDialog(
            title = "Удалить",
            message = "Удалить запись \"${item.productName}\"?",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteItem,
            onDismiss = viewModel::dismissDeleteConfirm,
            isDestructive = true
        )
    }
}

@Composable
private fun FinishedProductItem(
    item: FinishedProductListItemDto,
    onSell: () -> Unit,
    onWriteOff: () -> Unit,
    onReturn: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (item.status.lowercase()) {
        "instock" -> Success
        "sold" -> Primary
        "writtenoff" -> Warning
        else -> TextMuted
    }
    val statusText = when (item.status.lowercase()) {
        "instock" -> "На складе"
        "sold" -> "Продано"
        "writtenoff" -> "Списано"
        else -> item.status
    }
    val cost = if (item.costPerUnit > 0) item.costPerUnit else item.materialCost

    val fields = mutableListOf(
        CardField("ИЗДЕЛИЕ", item.productName),
    )
    if (item.batchNumber.isNotBlank()) {
        fields.add(CardField("ПАРТИЯ", item.batchNumber))
    }
    if (item.productionDate.isNotBlank()) {
        fields.add(CardField("ДАТА ПРОИЗВОДСТВА", item.productionDate.take(10)))
    }
    fields.add(CardField("СТАТУС", statusText, valueColor = statusColor))
    fields.add(CardField("СЕБЕСТОИМОСТЬ", fmtCur(cost)))
    if (item.recommendedPrice != null) {
        fields.add(CardField("РЕК. ЦЕНА", fmtCur(item.recommendedPrice)))
    }
    if (item.sellPrice != null) {
        fields.add(CardField("ЦЕНА ПРОДАЖИ", fmtCur(item.sellPrice), valueColor = Success))
    }
    if (!item.client.isNullOrBlank()) {
        fields.add(CardField("КЛИЕНТ", item.client, valueColor = Info))
    }
    if (!item.saleDate.isNullOrBlank()) {
        fields.add(CardField("ДАТА ПРОДАЖИ", item.saleDate.take(10)))
    }

    WebStyleCard(
        fields = fields,
        actions = {
            if (item.status.lowercase() == "instock") {
                IconButton(onClick = onSell) {
                    Icon(Icons.Default.AttachMoney, null, tint = Success, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onWriteOff) {
                    Icon(Icons.Default.Delete, null, tint = Warning, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, null, tint = Error, modifier = Modifier.size(22.dp))
                }
            } else {
                OutlinedButton(
                    onClick = onReturn,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Вернуть", color = TextSecondary, style = MaterialTheme.typography.labelMedium) }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, null, tint = Error, modifier = Modifier.size(22.dp))
                }
            }
        }
    )
}

private fun fmtCur(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 0
    return f.format(value)
}
