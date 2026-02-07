package com.workshop.mat.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(
            message = uiState.error!!,
            onRetry = viewModel::loadDashboard
        )
        else -> {
            val data = uiState.data ?: return
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Добро пожаловать!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Обзор состояния мастерской",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Материалы",
                        value = "${data.materialsSummary?.totalMaterials ?: 0}",
                        icon = Icons.Default.Inventory2,
                        iconTint = Primary,
                        modifier = Modifier.weight(1f),
                        subtitle = "${data.materialsSummary?.activeMaterials ?: 0} активных"
                    )
                    StatCard(
                        title = "Изделия",
                        value = "${data.productsSummary?.totalProducts ?: 0}",
                        icon = Icons.Default.Category,
                        iconTint = Secondary,
                        modifier = Modifier.weight(1f),
                        subtitle = "${data.productsSummary?.activeProducts ?: 0} активных"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "На складе",
                        value = "${data.finishedProductsSummary?.totalInStock ?: 0}",
                        icon = Icons.Default.ShoppingCart,
                        iconTint = Success,
                        modifier = Modifier.weight(1f),
                        subtitle = "единиц"
                    )
                    StatCard(
                        title = "Продано",
                        value = "${data.finishedProductsSummary?.totalSold ?: 0}",
                        icon = Icons.Default.TrendingUp,
                        iconTint = Warning,
                        modifier = Modifier.weight(1f),
                        subtitle = formatCurrency(data.finishedProductsSummary?.totalSalesRevenue ?: 0.0)
                    )
                }

                // Low stock materials
                AppCard {
                    Text(
                        text = "Заканчивающиеся материалы",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (data.lowStockMaterials.isNotEmpty()) {
                        data.lowStockMaterials.take(5).forEach { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(material.materialName, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Остаток: ${material.currentStock} ${material.unit}",
                                        color = Warning,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                StatusBadge(
                                    text = "Мин: ${material.minimumStock ?: 0} ${material.unit}",
                                    color = Warning,
                                    bgColor = WarningBg
                                )
                            }
                            if (material != data.lowStockMaterials.take(5).last()) {
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))
                            }
                        }
                    } else {
                        Text(
                            "Все материалы в достаточном количестве",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Recent operations
                AppCard {
                    Text(
                        text = "Последние операции",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (data.recentOperations.isNotEmpty()) {
                        data.recentOperations.take(5).forEach { op ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        op.operationTypeDisplay,
                                        color = Primary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        op.entityName ?: op.description ?: "",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (op.amount != null) {
                                    Text(
                                        formatCurrency(op.amount),
                                        color = Success,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (op != data.recentOperations.take(5).last()) {
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))
                            }
                        }
                    } else {
                        Text("Нет операций", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Quick stats
                AppCard {
                    Text(
                        text = "Стоимость склада",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(
                            (data.materialsSummary?.totalValue ?: 0.0) +
                            (data.finishedProductsSummary?.totalInStockValue ?: 0.0)
                        ),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "материалы + продукция",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    format.maximumFractionDigits = 0
    return format.format(value)
}
