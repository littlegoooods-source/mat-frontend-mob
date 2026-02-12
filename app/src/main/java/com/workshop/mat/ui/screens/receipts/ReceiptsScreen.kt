package com.workshop.mat.ui.screens.receipts

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
import com.workshop.mat.data.model.MaterialReceiptListItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptsScreen(viewModel: ReceiptsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = viewModel::openCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Добавить")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Filter by material
        if (uiState.materials.isNotEmpty()) {
            val materialNames = listOf("Все материалы") + uiState.materials.map { "${it.name}${if (it.color != null) " (${it.color})" else ""}" }
            AppDropdown(
                value = uiState.materialFilter?.let { id ->
                    uiState.materials.find { it.id == id }?.name ?: "Все материалы"
                } ?: "Все материалы",
                onValueChange = { name ->
                    val mat = uiState.materials.find { "${it.name}${if (it.color != null) " (${it.color})" else ""}" == name }
                    viewModel.updateMaterialFilter(mat?.id)
                },
                label = "Фильтр по материалу",
                options = materialNames
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.receipts.isEmpty() -> EmptyState("Нет приходов")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.receipts, key = { it.id }) { receipt ->
                    ReceiptItem(
                        receipt = receipt,
                        onEdit = { viewModel.openEditDialog(receipt) },
                        onDelete = { viewModel.showDeleteConfirm(receipt) }
                    )
                }
            }
        }
    }

    if (uiState.showDialog) {
        ReceiptFormDialog(uiState = uiState, viewModel = viewModel)
    }

    uiState.showDeleteConfirm?.let { receipt ->
        ConfirmDialog(
            title = "Удалить приход",
            message = "Удалить приход \"${receipt.materialName}\" (${receipt.quantity} ${receipt.materialUnit})?",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteReceipt,
            onDismiss = viewModel::dismissDeleteConfirm,
            isDestructive = true
        )
    }
}

@Composable
private fun ReceiptItem(
    receipt: MaterialReceiptListItemDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(shape = RoundedCornerShape(12.dp), color = DarkCard) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.materialName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    "${receipt.quantity} ${receipt.materialUnit} × ${formatCurrency(receipt.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary
                )
                if (!receipt.supplier.isNullOrBlank()) {
                    Text("Поставщик: ${receipt.supplier}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            Text(
                formatCurrency(receipt.totalPrice),
                style = MaterialTheme.typography.titleMedium,
                color = Success
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ReceiptFormDialog(uiState: ReceiptsUiState, viewModel: ReceiptsViewModel) {
    val materialOptions = uiState.materials.map { "${it.id}:${it.name}${if (it.color != null) " (${it.color})" else ""} - ${it.unit}" }

    FormDialog(
        title = if (uiState.editingReceipt != null) "Редактировать приход" else "Новый приход",
        onDismiss = viewModel::closeDialog
    ) {
        // Material picker
        AppDropdown(
            value = uiState.formMaterialId.let { id ->
                uiState.materials.find { it.id.toString() == id }?.let { "${it.name}${if (it.color != null) " (${it.color})" else ""}" } ?: "Выберите материал"
            },
            onValueChange = { display ->
                val mat = uiState.materials.find { "${it.name}${if (it.color != null) " (${it.color})" else ""}" == display }
                if (mat != null) viewModel.updateFormMaterialId(mat.id.toString())
            },
            label = "Материал",
            options = uiState.materials.map { "${it.name}${if (it.color != null) " (${it.color})" else ""}" }
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formQuantity, onValueChange = viewModel::updateFormQuantity, label = "Количество", keyboardType = KeyboardType.Decimal)
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formPricePerUnit, onValueChange = viewModel::updateFormPricePerUnit, label = "Цена за единицу", keyboardType = KeyboardType.Decimal)
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formSupplier, onValueChange = viewModel::updateFormSupplier, label = "Поставщик", placeholder = "Опционально")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formNotes, onValueChange = viewModel::updateFormNotes, label = "Примечание", placeholder = "Опционально", singleLine = false)
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = viewModel::saveReceipt,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isSaving
            ) { Text(if (uiState.editingReceipt != null) "Сохранить" else "Создать") }
            OutlinedButton(onClick = viewModel::closeDialog, modifier = Modifier.weight(1f)) {
                Text("Отмена", color = TextSecondary)
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 2
    return f.format(value)
}
