package com.workshop.mat.ui.screens.finished

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

data class FinishedProductsUiState(
    val items: List<FinishedProductListItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val statusFilter: String = "",
    // Sell dialog
    val showSellDialog: FinishedProductListItemDto? = null,
    val sellPrice: String = "",
    // Write-off dialog
    val showWriteOffDialog: FinishedProductListItemDto? = null,
    val writeOffReason: String = "",
    // Return confirm
    val showReturnConfirm: FinishedProductListItemDto? = null,
    // Delete confirm
    val showDeleteConfirm: FinishedProductListItemDto? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class FinishedProductsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinishedProductsUiState())
    val uiState: StateFlow<FinishedProductsUiState> = _uiState.asStateFlow()

    init { loadItems() }

    fun updateStatusFilter(v: String) { _uiState.value = _uiState.value.copy(statusFilter = v); loadItems() }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getFinishedProducts(status = _uiState.value.statusFilter.ifBlank { null })
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(items = response.body() ?: emptyList(), isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    // Sell
    fun openSellDialog(item: FinishedProductListItemDto) {
        _uiState.value = _uiState.value.copy(showSellDialog = item, sellPrice = "")
    }
    fun closeSellDialog() { _uiState.value = _uiState.value.copy(showSellDialog = null) }
    fun updateSellPrice(v: String) { _uiState.value = _uiState.value.copy(sellPrice = v) }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun sellProduct() {
        val item = _uiState.value.showSellDialog ?: return
        val price = _uiState.value.sellPrice.toDoubleOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                apiService.sellProduct(item.id, SellProductDto(price))
                _uiState.value = _uiState.value.copy(isSaving = false, showSellDialog = null)
                loadItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    snackbarMessage = e.localizedMessage ?: "Ошибка продажи"
                )
            }
        }
    }

    // Write-off
    fun openWriteOffDialog(item: FinishedProductListItemDto) {
        _uiState.value = _uiState.value.copy(showWriteOffDialog = item, writeOffReason = "")
    }
    fun closeWriteOffDialog() { _uiState.value = _uiState.value.copy(showWriteOffDialog = null) }
    fun updateWriteOffReason(v: String) { _uiState.value = _uiState.value.copy(writeOffReason = v) }

    fun writeOffProduct() {
        val item = _uiState.value.showWriteOffDialog ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                apiService.writeOffProduct(item.id, WriteOffProductDto(_uiState.value.writeOffReason.ifBlank { null }))
                _uiState.value = _uiState.value.copy(isSaving = false, showWriteOffDialog = null)
                loadItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    snackbarMessage = e.localizedMessage ?: "Ошибка списания"
                )
            }
        }
    }

    // Return to stock
    fun showReturnConfirm(item: FinishedProductListItemDto) { _uiState.value = _uiState.value.copy(showReturnConfirm = item) }
    fun dismissReturnConfirm() { _uiState.value = _uiState.value.copy(showReturnConfirm = null) }
    fun returnToStock() {
        val item = _uiState.value.showReturnConfirm ?: return
        viewModelScope.launch {
            try {
                apiService.returnToStock(item.id)
                _uiState.value = _uiState.value.copy(showReturnConfirm = null)
                loadItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showReturnConfirm = null,
                    snackbarMessage = e.localizedMessage ?: "Ошибка возврата на склад"
                )
            }
        }
    }

    // Delete
    fun showDeleteConfirm(item: FinishedProductListItemDto) { _uiState.value = _uiState.value.copy(showDeleteConfirm = item) }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }
    fun deleteItem() {
        val item = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            try {
                apiService.deleteFinishedProduct(item.id)
                _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
                loadItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirm = null,
                    snackbarMessage = e.localizedMessage ?: "Ошибка удаления"
                )
            }
        }
    }
}
