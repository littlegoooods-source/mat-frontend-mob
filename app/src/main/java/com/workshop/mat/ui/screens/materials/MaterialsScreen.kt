package com.workshop.mat.ui.screens.materials

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MaterialsScreen(viewModel: MaterialsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Материалы", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("Справочник материалов", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
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

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        SearchBar(value = uiState.search, onValueChange = viewModel::updateSearch)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.categories.isNotEmpty()) {
                AppDropdown(
                    value = uiState.categoryFilter.ifBlank { "Все" },
                    onValueChange = { viewModel.updateCategoryFilter(if (it == "Все") "" else it) },
                    label = "Категория",
                    options = listOf("Все") + uiState.categories,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.includeArchived,
                    onCheckedChange = { viewModel.toggleIncludeArchived() },
                    colors = CheckboxDefaults.colors(checkedColor = Primary, uncheckedColor = TextMuted)
                )
                Text("Архивные", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.materials.isEmpty() -> EmptyState("Нет материалов")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.materials, key = { it.id }) { material ->
                    MaterialItem(
                        material = material,
                        onEdit = { viewModel.openEditDialog(material) },
                        onArchive = { viewModel.archiveMaterial(material) },
                        onDelete = { viewModel.showDeleteConfirm(material) }
                    )
                }
            }
        }
    }

    // Create/Edit Dialog
    if (uiState.showDialog) {
        MaterialFormDialog(uiState = uiState, viewModel = viewModel)
    }

    // Delete Confirm
    uiState.showDeleteConfirm?.let { material ->
        ConfirmDialog(
            title = "Удалить материал",
            message = "Удалить материал \"${material.name}\"? Это действие нельзя отменить.",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteMaterial,
            onDismiss = viewModel::dismissDeleteConfirm,
            isDestructive = true
        )
    }
}

@Composable
private fun MaterialItem(
    material: com.workshop.mat.data.model.MaterialListItemDto,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(material.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    if (!material.color.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(text = material.color, color = TextSecondary, bgColor = DarkSurfaceVariant)
                    }
                }
                Text(
                    "${material.currentStock} ${material.unit} | ${material.category ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (material.isBelowMinimum) {
                    StatusBadge(text = "Мало", color = Warning, bgColor = WarningBg)
                } else if (material.isArchived) {
                    StatusBadge(text = "Архив", color = TextMuted, bgColor = DarkSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(material.averagePrice),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onArchive) {
                    Icon(
                        if (material.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = "Архив", tint = TextMuted, modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MaterialFormDialog(uiState: MaterialsUiState, viewModel: MaterialsViewModel) {
    val unitOptions = listOf("шт", "кг", "г", "м", "см", "л", "мл", "рулон", "упак")

    FormDialog(
        title = if (uiState.editingMaterial != null) "Редактировать материал" else "Новый материал",
        onDismiss = viewModel::closeDialog
    ) {
        AppTextField(value = uiState.formName, onValueChange = viewModel::updateFormName, label = "Название")
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(value = uiState.formUnit, onValueChange = viewModel::updateFormUnit, label = "Ед. изм.", options = unitOptions)
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formColor, onValueChange = viewModel::updateFormColor, label = "Цвет", placeholder = "Опционально")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formCategory, onValueChange = viewModel::updateFormCategory, label = "Категория", placeholder = "Например: Пластик")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = uiState.formMinimumStock, onValueChange = viewModel::updateFormMinimumStock,
            label = "Минимальный остаток",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formDescription, onValueChange = viewModel::updateFormDescription, label = "Описание", singleLine = false)
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = viewModel::saveMaterial,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.editingMaterial != null) "Сохранить" else "Создать")
            }
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
