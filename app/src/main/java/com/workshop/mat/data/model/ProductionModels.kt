package com.workshop.mat.data.model

data class ProductionListItemDto(
    val id: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val quantity: Int = 0,
    val status: String = "",
    val totalMaterialCost: Double = 0.0,
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
    val isAvailable: Boolean = false,
    val maxQuantity: Int = 0,
    val missingMaterials: List<MissingMaterialDto> = emptyList()
)

data class MissingMaterialDto(
    val materialId: Int = 0,
    val materialName: String = "",
    val materialUnit: String = "",
    val required: Double = 0.0,
    val available: Double = 0.0,
    val shortage: Double = 0.0
)
