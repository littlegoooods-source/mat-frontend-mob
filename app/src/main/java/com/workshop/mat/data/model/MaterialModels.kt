package com.workshop.mat.data.model

data class MaterialListItemDto(
    val id: Int = 0,
    val name: String = "",
    val unit: String = "",
    val color: String? = null,
    val category: String? = null,
    val description: String? = null,
    val minimumStock: Double? = null,
    val currentStock: Double = 0.0,
    val averagePrice: Double = 0.0,
    val totalValue: Double = 0.0,
    val isArchived: Boolean = false,
    val isBelowMinimum: Boolean = false
)

data class MaterialResponseDto(
    val id: Int = 0,
    val name: String = "",
    val unit: String = "",
    val color: String? = null,
    val category: String? = null,
    val description: String? = null,
    val minimumStock: Double? = null,
    val currentStock: Double = 0.0,
    val averagePrice: Double = 0.0,
    val isArchived: Boolean = false
)

data class MaterialCreateDto(
    val name: String,
    val unit: String,
    val color: String? = null,
    val category: String? = null,
    val description: String? = null,
    val minimumStock: Double? = null
)

data class MaterialUpdateDto(
    val name: String,
    val unit: String,
    val color: String? = null,
    val category: String? = null,
    val description: String? = null,
    val minimumStock: Double? = null
)

data class MaterialBalanceDto(
    val materialId: Int = 0,
    val materialName: String = "",
    val unit: String = "",
    val currentStock: Double = 0.0,
    val minimumStock: Double? = null,
    val averagePrice: Double = 0.0,
    val totalValue: Double = 0.0
)
