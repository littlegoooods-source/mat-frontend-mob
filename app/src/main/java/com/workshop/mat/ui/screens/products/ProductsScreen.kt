package com.workshop.mat.ui.screens.products

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.data.model.ProductListItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductsScreen(viewModel: ProductsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            SearchBar(value = uiState.search, onValueChange = viewModel::updateSearch)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        colors = CheckboxDefaults.colors(checkedColor = SelectionOrange, uncheckedColor = TextMuted)
                    )
                    Text("Архивные", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.products.isEmpty() -> EmptyState("Нет изделий")
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onEdit = { viewModel.openEditDialog(product) },
                            onCopy = { viewModel.openCopyDialog(product) },
                            onArchive = { viewModel.archiveProduct(product) },
                            onDelete = { viewModel.showDeleteConfirm(product) }
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
            Icon(Icons.Default.Add, contentDescription = "Добавить")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)
        )
    }

    // Create/Edit dialog
    if (uiState.showDialog) {
        ProductFormDialog(uiState = uiState, viewModel = viewModel)
    }

    // Copy dialog
    if (uiState.showCopyDialog) {
        SmallDialog(title = "Копировать изделие", onDismiss = viewModel::closeCopyDialog) {
            AppTextField(value = uiState.copyName, onValueChange = viewModel::updateCopyName, label = "Название копии")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::copyProduct,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Копировать") }
                OutlinedButton(onClick = viewModel::closeCopyDialog, modifier = Modifier.weight(1f)) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        }
    }

    // Delete confirm
    uiState.showDeleteConfirm?.let { product ->
        ConfirmDialog(
            title = "Удалить изделие",
            message = "Удалить \"${product.name}\"? Это действие нельзя отменить.",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteProduct,
            onDismiss = viewModel::dismissDeleteConfirm,
            isDestructive = true
        )
    }
}

@Composable
private fun ProductItem(
    product: ProductListItemDto,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(shape = RoundedCornerShape(12.dp), color = DarkCard) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(product.category ?: "-", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    if (product.weight > 0) Text("${product.weight} кг", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (product.estimatedCost != null) Text("Себест: ${fmtCur(product.estimatedCost)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    if (product.recommendedPrice != null) Text("Цена: ${fmtCur(product.recommendedPrice)}", style = MaterialTheme.typography.bodySmall, color = Success)
                }
                if (product.inStockCount > 0) {
                    StatusBadge(text = "${product.inStockCount} шт на складе", color = Success, bgColor = SuccessBg)
                }
                if (product.isArchived) {
                    StatusBadge(text = "Архив", color = TextMuted, bgColor = DarkSurfaceVariant)
                }
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = TextMuted, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onCopy) { Icon(Icons.Default.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(20.dp)) }
            }
            Column {
                IconButton(onClick = onArchive) {
                    Icon(if (product.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(uiState: ProductsUiState, viewModel: ProductsViewModel) {
    FormDialog(
        title = if (uiState.editingProduct != null) "Редактировать изделие" else "Новое изделие",
        onDismiss = viewModel::closeDialog
    ) {
        AppTextField(value = uiState.formName, onValueChange = viewModel::updateFormName, label = "Название")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formCategory, onValueChange = viewModel::updateFormCategory, label = "Категория")
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(value = uiState.formEstimatedCost, onValueChange = viewModel::updateFormEstimatedCost, label = "Себестоимость", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
            AppTextField(value = uiState.formRecommendedPrice, onValueChange = viewModel::updateFormRecommendedPrice, label = "Рек. цена", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formProductionTime, onValueChange = viewModel::updateFormProductionTime, label = "Время производства (мин)", keyboardType = KeyboardType.Number)
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formDescription, onValueChange = viewModel::updateFormDescription, label = "Описание", singleLine = false)
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = uiState.formFileLinks, onValueChange = viewModel::updateFormFileLinks, label = "Ссылки на файлы")

        Spacer(modifier = Modifier.height(16.dp))

        // Recipe items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Материалы в рецепте", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            TextButton(onClick = viewModel::addRecipeItem) {
                Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(18.dp))
                Text("Добавить", color = Primary)
            }
        }

        if (uiState.formRecipeItems.isEmpty()) {
            Text("Нет материалов. Нажмите \"Добавить\".", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        } else {
            uiState.formRecipeItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppDropdown(
                        value = uiState.materials.find { it.id.toString() == item.materialId }?.let {
                            "${it.name}${if (it.color != null) " (${it.color})" else ""}"
                        } ?: "Материал",
                        onValueChange = { display ->
                            val mat = uiState.materials.find { "${it.name}${if (it.color != null) " (${it.color})" else ""}" == display }
                            if (mat != null) viewModel.updateRecipeItem(index, materialId = mat.id.toString())
                        },
                        label = "Материал",
                        options = uiState.materials.map { "${it.name}${if (it.color != null) " (${it.color})" else ""}" },
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = item.quantity,
                        onValueChange = { viewModel.updateRecipeItem(index, quantity = it) },
                        label = "Кол-во",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.width(80.dp)
                    )
                    IconButton(onClick = { viewModel.removeRecipeItem(index) }) {
                        Icon(Icons.Default.Close, null, tint = Error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Auto-calculated weight
        if (uiState.formWeight.isNotBlank() && (uiState.formWeight.toDoubleOrNull() ?: 0.0) > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = InfoBg) {
                Text(
                    "Вес изделия (авто): ${"%.2f".format(uiState.formWeight.toDoubleOrNull() ?: 0.0)} кг",
                    color = Info,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = viewModel::saveProduct,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isSaving
            ) { Text(if (uiState.editingProduct != null) "Сохранить" else "Создать") }
            OutlinedButton(onClick = viewModel::closeDialog, modifier = Modifier.weight(1f)) {
                Text("Отмена", color = TextSecondary)
            }
        }
    }
}

private fun fmtCur(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 0
    return f.format(value)
}
