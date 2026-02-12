package com.workshop.mat.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.navigation.Routes
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private data class NavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

private val navItems = listOf(
    NavItem("Материалы", Icons.Default.Inventory2, Routes.MATERIALS, Primary),
    NavItem("Изделия", Icons.Default.Category, Routes.PRODUCTS, Secondary),
    NavItem("Производство", Icons.Default.Factory, Routes.PRODUCTIONS, Success),
    NavItem("Приходы", Icons.Default.LocalShipping, Routes.RECEIPTS, Info),
    NavItem("Продукция", Icons.Default.ShoppingCart, Routes.FINISHED_PRODUCTS, Warning),
    NavItem("История", Icons.Default.History, Routes.HISTORY, Color(0xFF8B5CF6)),
    NavItem("Настройки", Icons.Default.Settings, Routes.SETTINGS, TextMuted),
)

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Мастерская",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = TextPrimary
        )
        if (uiState.userName.isNotBlank()) {
            Text(
                text = "Привет, ${uiState.userName}!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Navigation grid — 2 columns
        Spacer(modifier = Modifier.height(4.dp))

        // Row 1
        for (rowIndex in navItems.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NavCard(
                    item = navItems[rowIndex],
                    onClick = { onNavigate(navItems[rowIndex].route) },
                    modifier = Modifier.weight(1f)
                )
                if (rowIndex + 1 < navItems.size) {
                    NavCard(
                        item = navItems[rowIndex + 1],
                        onClick = { onNavigate(navItems[rowIndex + 1].route) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats section
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            uiState.error != null -> {
                ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadDashboard)
            }
            uiState.data != null -> {
                val data = uiState.data!!

                // Quick stats row
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
                        title = "На складе",
                        value = "${data.finishedProductsSummary?.totalInStock ?: 0}",
                        icon = Icons.Default.ShoppingCart,
                        iconTint = Success,
                        modifier = Modifier.weight(1f),
                        subtitle = "единиц"
                    )
                }

                // Low stock materials
                if (data.lowStockMaterials.isNotEmpty()) {
                    AppCard {
                        Text(
                            text = "Заканчивающиеся материалы",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        data.lowStockMaterials.take(3).forEach { material ->
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
                                    text = "Мин: ${material.minimumStock ?: 0}",
                                    color = Warning,
                                    bgColor = WarningBg
                                )
                            }
                            if (material != data.lowStockMaterials.take(3).last()) {
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                // Warehouse value
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NavCard(
    item: NavItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = DarkCard,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = item.color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    format.maximumFractionDigits = 0
    return format.format(value)
}
