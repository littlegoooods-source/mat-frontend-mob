package com.workshop.mat.data.model

data class OperationHistoryItemDto(
    val id: Int = 0,
    val operationType: String = "",
    val operationTypeDisplay: String = "",
    val entityType: String = "",
    val entityId: Int? = null,
    val entityName: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val isCancelled: Boolean = false,
    val createdAt: String = "",
    val userName: String? = null
)

data class PagedResultDto<T>(
    val items: List<T> = emptyList(),
    val totalCount: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 50,
    val totalPages: Int = 0
)

data class MaterialReceiptListItemDto(
    val id: Int = 0,
    val materialId: Int = 0,
    val materialName: String = "",
    val materialUnit: String = "",
    val quantity: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val supplier: String? = null,
    val purchaseSource: String? = null,
    val batchNumber: String? = null,
    val notes: String? = null,
    val comment: String? = null,
    val remainingQuantity: Double = 0.0,
    val hasUsedMaterials: Boolean = false,
    val receiptDate: String = "",
    val createdAt: String = ""
)

data class MaterialReceiptResponseDto(
    val id: Int = 0,
    val materialId: Int = 0,
    val materialName: String = "",
    val quantity: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val supplier: String? = null,
    val purchaseSource: String? = null,
    val batchNumber: String? = null,
    val notes: String? = null,
    val comment: String? = null,
    val receiptDate: String = "",
    val createdAt: String = ""
)

data class MaterialReceiptCreateDto(
    val materialId: Int,
    val quantity: Double,
    val pricePerUnit: Double,
    val unitPrice: Double? = null,
    val totalPrice: Double? = null,
    val supplier: String? = null,
    val purchaseSource: String? = null,
    val batchNumber: String? = null,
    val notes: String? = null,
    val comment: String? = null,
    val receiptDate: String? = null
)

data class MaterialReceiptUpdateDto(
    val materialId: Int,
    val quantity: Double,
    val pricePerUnit: Double,
    val unitPrice: Double? = null,
    val totalPrice: Double? = null,
    val supplier: String? = null,
    val purchaseSource: String? = null,
    val batchNumber: String? = null,
    val notes: String? = null,
    val comment: String? = null,
    val receiptDate: String? = null
)
