package com.workshop.mat.ui.screens.history

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
import com.workshop.mat.data.model.OperationHistoryItemDto
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.includeCancelled,
                    onCheckedChange = { viewModel.toggleIncludeCancelled() },
                    colors = CheckboxDefaults.colors(checkedColor = Primary, uncheckedColor = TextMuted)
                )
                Text("Отменённые", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.items.isEmpty() -> EmptyState("Нет операций")
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        HistoryItem(item)
                    }
                }

                // Pagination
                if (uiState.totalPages > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::prevPage, enabled = uiState.page > 1) {
                            Icon(Icons.Default.ChevronLeft, null, tint = if (uiState.page > 1) Primary else TextMuted)
                        }
                        Text(
                            "${uiState.page} / ${uiState.totalPages}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        IconButton(onClick = viewModel::nextPage, enabled = uiState.page < uiState.totalPages) {
                            Icon(Icons.Default.ChevronRight, null, tint = if (uiState.page < uiState.totalPages) Primary else TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: OperationHistoryItemDto) {
    Surface(shape = RoundedCornerShape(10.dp), color = DarkCard) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        item.operationTypeDisplay,
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary
                    )
                    if (item.isCancelled) {
                        StatusBadge(text = "Отменено", color = Error, bgColor = ErrorBg)
                    }
                }
                Text(
                    item.entityName ?: item.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (item.quantity != null) {
                    Text(
                        "${item.quantity} ${item.unit ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (item.amount != null) {
                    Text(
                        fmtCur(item.amount),
                        style = MaterialTheme.typography.labelMedium,
                        color = Success
                    )
                }
                Text(
                    formatDate(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

private fun fmtCur(value: Double): String {
    val f = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    f.maximumFractionDigits = 0
    return f.format(value)
}

private fun formatDate(dateStr: String): String {
    return try {
        // Parse ISO date and format
        val parts = dateStr.take(16).split("T")
        if (parts.size == 2) {
            val datePart = parts[0].split("-")
            val timePart = parts[1]
            if (datePart.size == 3) "${datePart[2]}.${datePart[1]}.${datePart[0]} $timePart"
            else dateStr.take(16)
        } else dateStr.take(16)
    } catch (e: Exception) {
        dateStr.take(16)
    }
}
