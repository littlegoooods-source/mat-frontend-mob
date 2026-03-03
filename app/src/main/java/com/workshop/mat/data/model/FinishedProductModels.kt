package com.workshop.mat.data.model

data class FinishedProductListItemDto(
    val id: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val productionId: Int? = null,
    val batchNumber: String = "",
    val productionDate: String = "",
    val status: String = "",
    val materialCost: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val sellPrice: Double? = null,
    val recommendedPrice: Double? = null,
    val client: String? = null,
    val saleDate: String? = null,
    val createdAt: String = "",
    val soldAt: String? = null,
    val writtenOffAt: String? = null
)

data class FinishedProductResponseDto(
    val id: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val productionId: Int? = null,
    val status: String = "",
    val materialCost: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val sellPrice: Double? = null,
    val profit: Double? = null,
    val createdAt: String = "",
    val soldAt: String? = null,
    val writtenOffAt: String? = null,
    val writeOffReason: String? = null,
    val notes: String? = null
)

data class FinishedProductUpdateDto(
    val sellPrice: Double? = null,
    val notes: String? = null
)

data class SellProductDto(
    val sellPrice: Double
)

data class WriteOffProductDto(
    val reason: String? = null
)

data class FinishedProductSummaryDto(
    val totalInStock: Int = 0,
    val totalSold: Int = 0,
    val totalWrittenOff: Int = 0,
    val totalInStockValue: Double = 0.0,
    val totalSalesRevenue: Double = 0.0,
    val totalSalesAmount: Double = 0.0,
    val totalProfit: Double = 0.0
)
