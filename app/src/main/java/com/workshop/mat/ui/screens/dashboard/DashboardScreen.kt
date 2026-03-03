package com.workshop.mat.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.navigation.Routes
import com.workshop.mat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// Navigation items for the bottom toolbar
private data class ToolbarNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

private val toolbarItems = listOf(
    ToolbarNavItem("Материалы", Icons.Default.Inventory2, Routes.MATERIALS, Primary),
    ToolbarNavItem("Поступления", Icons.Default.LocalShipping, Routes.RECEIPTS, Info),
    ToolbarNavItem("Изделия", Icons.Default.Category, Routes.PRODUCTS, Secondary),
    ToolbarNavItem("Производство", Icons.Default.Factory, Routes.PRODUCTIONS, Success),
    ToolbarNavItem("Продукция", Icons.Default.ShoppingCart, Routes.FINISHED_PRODUCTS, Warning),
    ToolbarNavItem("История", Icons.Default.History, Routes.HISTORY, Color(0xFF8B5CF6)),
    ToolbarNavItem("Настройки", Icons.Default.Settings, Routes.SETTINGS, TextMuted),
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
            .background(DarkBackground)
    ) {
        // Scrollable content area (takes all space except toolbar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "Мастерская",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
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
                    val matSummary = data.materialsSummary
                    val fpSummary = data.finishedProductsSummary

                    // =============== TOP STAT CARDS (2x2 grid) ===============
                    // Row 1: Materials on stock + Finished products
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardStatCard(
                            title = "Материалы\nна складе",
                            value = formatCurrency(matSummary?.totalValue ?: 0.0),
                            subtitle = "${matSummary?.activeMaterials ?: 0} позиций",
                            icon = Icons.Default.Inventory2,
                            iconColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Готовая\nпродукция",
                            value = "${fpSummary?.totalInStock ?: 0}",
                            subtitle = formatCurrency(fpSummary?.totalInStockValue ?: 0.0),
                            icon = Icons.Default.Factory,
                            iconColor = Success,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Sold + Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardStatCard(
                            title = "Продано",
                            value = "${fpSummary?.totalSold ?: 0}",
                            subtitle = formatCurrency(fpSummary?.totalSalesRevenue ?: 0.0),
                            icon = Icons.Default.ShoppingCart,
                            iconColor = Info,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Прибыль",
                            value = formatCurrency(fpSummary?.totalProfit ?: 0.0),
                            subtitle = "с продаж",
                            icon = Icons.Default.TrendingUp,
                            iconColor = Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // =============== SALES CHART ===============
                    Spacer(modifier = Modifier.height(4.dp))
                    SalesChartSection(
                        uiState = uiState,
                        onPeriodChange = viewModel::selectPeriod
                    )
                }
            }
        }

        // =============== BOTTOM TOOLBAR ===============
        HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), thickness = 1.dp)
        BottomToolbar(
            items = toolbarItems,
            onNavigate = onNavigate
        )
    }
}

// ======================== STAT CARD ========================

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkCard,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ======================== SALES CHART SECTION ========================

@Composable
private fun SalesChartSection(
    uiState: DashboardUiState,
    onPeriodChange: (SalesPeriod) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCard,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title + Period selector
            Text(
                text = "Продажи",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )

            // Period tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SalesPeriod.entries.forEach { period ->
                    val selected = uiState.selectedPeriod == period
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPeriodChange(period) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) SelectionOrange else DarkSurfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = period.label,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Chart content
            when {
                uiState.isSalesLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                    }
                }
                uiState.salesData != null -> {
                    val salesData = uiState.salesData!!

                    // Summary row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Выручка", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                formatCurrency(salesData.totalRevenue),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Прибыль", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                formatCurrency(salesData.totalProfit),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Success
                            )
                        }
                    }

                    // Line chart showing sales over time
                    if (uiState.salesTimeSeries.any { it.salesCount > 0 }) {
                        SalesLineChart(
                            points = uiState.salesTimeSeries,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Нет данных за период",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (salesData.totalSales > 0) {
                        Text(
                            text = "Всего продаж: ${salesData.totalSales}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Не удалось загрузить данные",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// ======================== LINE CHART ========================

@Composable
private fun SalesLineChart(
    points: List<SalesTimePoint>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textColorInt = android.graphics.Color.argb(180, 148, 163, 184)

    Column {
        Canvas(modifier = modifier) {
            if (points.size < 2) return@Canvas

            val maxSales = points.maxOf { it.salesCount }.coerceAtLeast(1)
            val leftPad = 40f
            val chartLeft = leftPad
            val chartRight = size.width - 10f
            val chartTop = 20f
            val chartBottom = size.height - 40f
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            // Grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = chartTop + chartHeight * i / gridLines
                drawLine(
                    color = DarkBorder.copy(alpha = 0.15f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 1f
                )
                val value = maxSales - maxSales * i / gridLines
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = textColorInt
                        textSize = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    drawText(value.toString(), chartLeft - 6f, y + with(density) { 3.sp.toPx() }, paint)
                }
            }

            // Build line path
            val step = chartWidth / (points.size - 1).coerceAtLeast(1)
            val linePoints = points.mapIndexed { i, p ->
                val x = chartLeft + step * i
                val y = chartTop + chartHeight * (1f - p.salesCount.toFloat() / maxSales)
                Offset(x, y)
            }

            // Fill area under curve
            val fillPath = Path().apply {
                moveTo(linePoints.first().x, chartBottom)
                linePoints.forEach { lineTo(it.x, it.y) }
                lineTo(linePoints.last().x, chartBottom)
                close()
            }
            drawPath(fillPath, Primary.copy(alpha = 0.12f))

            // Draw line
            val linePath = Path().apply {
                linePoints.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(linePath, Primary, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // Draw dots
            linePoints.forEach { pt ->
                drawCircle(Primary, radius = 5f, center = pt)
                drawCircle(DarkCard, radius = 3f, center = pt)
            }

            // X-axis labels
            val labelStep = if (points.size > 7) 2 else 1
            points.forEachIndexed { i, p ->
                if (i % labelStep == 0 || i == points.size - 1) {
                    val x = chartLeft + step * i
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = textColorInt
                            textSize = with(density) { 8.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(p.label, x, chartBottom + with(density) { 13.sp.toPx() }, paint)
                    }
                }
            }

            // Bottom axis line
            drawLine(
                color = DarkBorder.copy(alpha = 0.3f),
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartRight, chartBottom),
                strokeWidth = 1f
            )
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Primary.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Количество продаж", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

// ======================== BOTTOM TOOLBAR ========================

@Composable
private fun BottomToolbar(
    items: List<ToolbarNavItem>,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = DarkSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "Основные возможности",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    ToolbarItem(
                        item = item,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarItem(
    item: ToolbarNavItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = item.color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ======================== HELPERS ========================

private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    format.maximumFractionDigits = 0
    return format.format(value)
}
