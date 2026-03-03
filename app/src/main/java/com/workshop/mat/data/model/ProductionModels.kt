package com.workshop.mat.data.model

data class ProductionListItemDto(
    val id: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val quantity: Int = 0,
    val status: String = "",
    val totalMaterialCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val recommendedPricePerUnit: Double? = null,
    val batchNumber: String = "",
    val productionDate: String = "",
    val inStockCount: Int = 0,
    val isCancelled: Boolean = false,
    val createdAt: String = "",
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val notes: String? = null
)

data class ProductionResponseDto(
    val id: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val quantity: Int = 0,
    val status: String = "",
    val totalMaterialCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val materialsUsed: List<ProductionMaterialDto> = emptyList(),
    val createdAt: String = "",
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val notes: String? = null
)

data class ProductionMaterialDto(
    val materialId: Int = 0,
    val materialName: String = "",
    val materialUnit: String = "",
    val quantityUsed: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val totalCost: Double = 0.0
)

data class ProductionCreateDto(
    val productId: Int,
    val quantity: Int,
    val notes: String? = null
)

data class ProductionAvailabilityDto(
    val canProduce: Boolean = false,
    val isAvailable: Boolean = false,
    val maxQuantity: Int = 0,
    val estimatedCostPerUnit: Double = 0.0,
    val estimatedTotalCost: Double = 0.0,
    val materials: List<AvailabilityMaterialDto> = emptyList(),
    val missingMaterials: List<AvailabilityMaterialDto> = emptyList()
)

data class AvailabilityMaterialDto(
    val materialId: Int = 0,
    val materialName: String = "",
    val materialUnit: String = "",
    val requiredQuantity: Double = 0.0,
    val availableQuantity: Double = 0.0,
    val required: Double = 0.0,
    val available: Double = 0.0,
    val isAvailable: Boolean = true,
    val shortage: Double = 0.0
)
