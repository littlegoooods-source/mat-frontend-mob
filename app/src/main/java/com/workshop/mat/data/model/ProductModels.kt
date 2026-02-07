package com.workshop.mat.data.model

data class ProductListItemDto(
    val id: Int = 0,
    val name: String = "",
    val category: String? = null,
    val description: String? = null,
    val productionTimeMinutes: Int = 0,
    val weight: Double = 0.0,
    val estimatedCost: Double? = null,
    val recommendedPrice: Double? = null,
    val markupPercent: Double = 100.0,
    val inStockCount: Int = 0,
    val isArchived: Boolean = false
)

data class ProductResponseDto(
    val id: Int = 0,
    val name: String = "",
    val category: String? = null,
    val description: String? = null,
    val productionTimeMinutes: Int = 0,
    val weight: Double = 0.0,
    val estimatedCost: Double? = null,
    val recommendedPrice: Double? = null,
    val fileLinks: String? = null,
    val markupPercent: Double = 100.0,
    val recipeItems: List<RecipeItemDto> = emptyList(),
    val isArchived: Boolean = false
)

data class RecipeItemDto(
    val materialId: Int = 0,
    val materialName: String? = null,
    val materialUnit: String? = null,
    val quantity: Double = 0.0
)

data class ProductCreateDto(
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val productionTimeMinutes: Int = 0,
    val weight: Double = 0.0,
    val estimatedCost: Double? = null,
    val recommendedPrice: Double? = null,
    val fileLinks: String? = null,
    val markupPercent: Double = 100.0,
    val recipeItems: List<RecipeItemCreateDto> = emptyList()
)

data class ProductUpdateDto(
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val productionTimeMinutes: Int = 0,
    val weight: Double = 0.0,
    val estimatedCost: Double? = null,
    val recommendedPrice: Double? = null,
    val fileLinks: String? = null,
    val markupPercent: Double = 100.0,
    val recipeItems: List<RecipeItemCreateDto> = emptyList()
)

data class RecipeItemCreateDto(
    val materialId: Int,
    val quantity: Double
)

data class ProductCopyDto(
    val newName: String
)
