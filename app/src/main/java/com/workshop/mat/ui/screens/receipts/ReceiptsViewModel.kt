package com.workshop.mat.ui.screens.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptsUiState(
    val receipts: List<MaterialReceiptListItemDto> = emptyList(),
    val materials: List<MaterialListItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val materialFilter: Int? = null,
    val showDialog: Boolean = false,
    val editingReceipt: MaterialReceiptListItemDto? = null,
    val formMaterialId: String = "",
    val formQuantity: String = "",
    val formPricePerUnit: String = "",
    val formTotalPrice: String = "",
    val formReceiptDate: String = "",
    val formBatchNumber: String = "",
    val formSupplier: String = "",
    val formNotes: String = "",
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null,
    val showDeleteConfirm: MaterialReceiptListItemDto? = null
)

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    init {
        loadReceipts()
        loadMaterials()
    }

    fun loadReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getReceipts(materialId = _uiState.value.materialFilter)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(receipts = response.body() ?: emptyList(), isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun loadMaterials() {
        viewModelScope.launch {
            try {
                val response = apiService.getMaterials()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(materials = response.body() ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun updateMaterialFilter(id: Int?) {
        _uiState.value = _uiState.value.copy(materialFilter = id)
        loadReceipts()
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showDialog = true, editingReceipt = null,
            formMaterialId = "", formQuantity = "", formPricePerUnit = "",
            formTotalPrice = "", formReceiptDate = "", formBatchNumber = "",
            formSupplier = "", formNotes = ""
        )
    }

    fun openEditDialog(receipt: MaterialReceiptListItemDto) {
        _uiState.value = _uiState.value.copy(
            showDialog = true, editingReceipt = receipt,
            formMaterialId = receipt.materialId.toString(),
            formQuantity = receipt.quantity.toString(),
            formPricePerUnit = receipt.pricePerUnit.toString(),
            formTotalPrice = receipt.totalPrice.toString(),
            formReceiptDate = receipt.receiptDate,
            formBatchNumber = receipt.batchNumber ?: "",
            formSupplier = receipt.purchaseSource ?: receipt.supplier ?: "",
            formNotes = receipt.comment ?: receipt.notes ?: ""
        )
    }

    fun closeDialog() { _uiState.value = _uiState.value.copy(showDialog = false) }

    fun updateFormMaterialId(v: String) { _uiState.value = _uiState.value.copy(formMaterialId = v) }
    fun updateFormQuantity(v: String) {
        _uiState.value = _uiState.value.copy(formQuantity = v)
        recalculateTotalPrice()
    }
    fun updateFormPricePerUnit(v: String) {
        _uiState.value = _uiState.value.copy(formPricePerUnit = v)
        recalculateTotalPrice()
    }
    fun updateFormTotalPrice(v: String) { _uiState.value = _uiState.value.copy(formTotalPrice = v) }
    fun updateFormReceiptDate(v: String) { _uiState.value = _uiState.value.copy(formReceiptDate = v) }
    fun updateFormBatchNumber(v: String) { _uiState.value = _uiState.value.copy(formBatchNumber = v) }
    fun updateFormSupplier(v: String) { _uiState.value = _uiState.value.copy(formSupplier = v) }
    fun updateFormNotes(v: String) { _uiState.value = _uiState.value.copy(formNotes = v) }
    fun clearSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }

    private fun recalculateTotalPrice() {
        val qty = _uiState.value.formQuantity.toDoubleOrNull() ?: 0.0
        val price = _uiState.value.formPricePerUnit.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(formTotalPrice = "%.2f".format(qty * price))
    }

    fun saveReceipt() {
        val s = _uiState.value
        val materialId = s.formMaterialId.toIntOrNull()
        if (materialId == null) {
            _uiState.value = s.copy(snackbarMessage = "Выберите материал")
            return
        }
        val quantity = s.formQuantity.toDoubleOrNull()
        if (quantity == null) {
            _uiState.value = s.copy(snackbarMessage = "Укажите количество")
            return
        }
        val price = s.formPricePerUnit.toDoubleOrNull()
        if (price == null) {
            _uiState.value = s.copy(snackbarMessage = "Укажите цену за единицу")
            return
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            try {
                if (s.editingReceipt != null) {
                    apiService.updateReceipt(s.editingReceipt.id, MaterialReceiptUpdateDto(
                        materialId = materialId, quantity = quantity, pricePerUnit = price,
                        unitPrice = price,
                        totalPrice = s.formTotalPrice.toDoubleOrNull(),
                        supplier = s.formSupplier.ifBlank { null },
                        purchaseSource = s.formSupplier.ifBlank { null },
                        batchNumber = s.formBatchNumber.ifBlank { null },
                        notes = s.formNotes.ifBlank { null },
                        comment = s.formNotes.ifBlank { null },
                        receiptDate = s.formReceiptDate.ifBlank { null }
                    ))
                } else {
                    apiService.createReceipt(MaterialReceiptCreateDto(
                        materialId = materialId, quantity = quantity, pricePerUnit = price,
                        unitPrice = price,
                        totalPrice = s.formTotalPrice.toDoubleOrNull(),
                        supplier = s.formSupplier.ifBlank { null },
                        purchaseSource = s.formSupplier.ifBlank { null },
                        batchNumber = s.formBatchNumber.ifBlank { null },
                        notes = s.formNotes.ifBlank { null },
                        comment = s.formNotes.ifBlank { null },
                        receiptDate = s.formReceiptDate.ifBlank { null }
                    ))
                }
                _uiState.value = _uiState.value.copy(isSaving = false, showDialog = false)
                loadReceipts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, snackbarMessage = e.localizedMessage ?: "Ошибка сохранения")
            }
        }
    }

    fun showDeleteConfirm(receipt: MaterialReceiptListItemDto) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = receipt)
    }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }

    fun deleteReceipt() {
        val receipt = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            try {
                apiService.deleteReceipt(receipt.id)
                _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
                loadReceipts()
            } catch (_: Exception) {}
        }
    }
}
