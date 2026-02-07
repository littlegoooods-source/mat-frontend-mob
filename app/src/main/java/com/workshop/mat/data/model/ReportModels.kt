package com.workshop.mat.data.model

data class DashboardDto(
    val materialsSummary: MaterialsSummaryDto? = null,
    val productsSummary: ProductsSummaryDto? = null,
    val finishedProductsSummary: FinishedProductsSummaryDto? = null,
    val lowStockMaterials: List<MaterialBalanceDto> = emptyList(),
    val recentOperations: List<OperationHistoryItemDto> = emptyList()
)

data class MaterialsSummaryDto(
    val totalMaterials: Int = 0,
    val activeMaterials: Int = 0,
    val totalValue: Double = 0.0,
    val lowStockCount: Int = 0
)

data class ProductsSummaryDto(
    val totalProducts: Int = 0,
    val activeProducts: Int = 0,
    val totalProduced: Int = 0
)

data class FinishedProductsSummaryDto(
    val totalInStock: Int = 0,
    val totalSold: Int = 0,
    val totalWrittenOff: Int = 0,
    val totalInStockValue: Double = 0.0,
    val totalSalesRevenue: Double = 0.0,
    val totalProfit: Double = 0.0
)
