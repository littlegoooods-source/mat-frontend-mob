package com.workshop.mat.ui.screens.productions

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

data class ProductionsUiState(
    val productions: List<ProductionListItemDto> = emptyList(),
    val products: List<ProductListItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val statusFilter: String = "",
    // Create dialog
    val showCreateDialog: Boolean = false,
    val formProductId: String = "",
    val formQuantity: String = "1",
    val formNotes: String = "",
    val availability: ProductionAvailabilityDto? = null,
    val isCheckingAvailability: Boolean = false,
    val isSaving: Boolean = false,
    // Cancel / delete
    val showCancelConfirm: ProductionListItemDto? = null,
    val showDeleteConfirm: ProductionListItemDto? = null,
    // Snackbar
    val snackbarMessage: String? = null
)

@HiltViewModel
class ProductionsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductionsUiState())
    val uiState: StateFlow<ProductionsUiState> = _uiState.asStateFlow()

    init {
        loadProductions()
        loadProducts()
    }

    fun clearSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }

    fun updateStatusFilter(v: String) { _uiState.value = _uiState.value.copy(statusFilter = v); loadProductions() }

    fun loadProductions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getProductions(
                    status = _uiState.value.statusFilter.ifBlank { null }
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(productions = response.body() ?: emptyList(), isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                val response = apiService.getProducts(includeArchived = false)
                if (response.isSuccessful) _uiState.value = _uiState.value.copy(products = response.body() ?: emptyList())
            } catch (_: Exception) {}
        }
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true, formProductId = "", formQuantity = "1",
            formNotes = "", availability = null
        )
    }
    fun closeCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }

    fun updateFormProductId(v: String) {
        _uiState.value = _uiState.value.copy(formProductId = v, availability = null)
        checkAvailability()
    }
    fun updateFormQuantity(v: String) {
        _uiState.value = _uiState.value.copy(formQuantity = v, availability = null)
        checkAvailability()
    }
    fun updateFormNotes(v: String) { _uiState.value = _uiState.value.copy(formNotes = v) }

    private fun checkAvailability() {
        val productId = _uiState.value.formProductId.toIntOrNull() ?: return
        val quantity = _uiState.value.formQuantity.toIntOrNull() ?: return
        if (quantity <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingAvailability = true)
            try {
                val response = apiService.checkAvailability(productId, quantity)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(availability = response.body(), isCheckingAvailability = false)
                } else {
                    _uiState.value = _uiState.value.copy(isCheckingAvailability = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isCheckingAvailability = false)
            }
        }
    }

    fun createProduction() {
        val s = _uiState.value
        val productId = s.formProductId.toIntOrNull() ?: return
        val quantity = s.formQuantity.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            try {
                val response = apiService.createProduction(ProductionCreateDto(
                    productId = productId, quantity = quantity,
                    notes = s.formNotes.ifBlank { null }
                ))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, showCreateDialog = false)
                    loadProductions()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        snackbarMessage = "Ошибка создания производства"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    snackbarMessage = e.localizedMessage ?: "Ошибка создания производства"
                )
            }
        }
    }

    fun showCancelConfirm(p: ProductionListItemDto) { _uiState.value = _uiState.value.copy(showCancelConfirm = p) }
    fun dismissCancelConfirm() { _uiState.value = _uiState.value.copy(showCancelConfirm = null) }
    fun cancelProduction() {
        val p = _uiState.value.showCancelConfirm ?: return
        viewModelScope.launch {
            try {
                val response = apiService.cancelProduction(p.id)
                _uiState.value = _uiState.value.copy(showCancelConfirm = null)
                if (response.isSuccessful) {
                    loadProductions()
                } else {
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Ошибка отмены производства")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showCancelConfirm = null,
                    snackbarMessage = e.localizedMessage ?: "Ошибка отмены производства"
                )
            }
        }
    }

    fun showDeleteConfirm(p: ProductionListItemDto) { _uiState.value = _uiState.value.copy(showDeleteConfirm = p) }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }
    fun deleteProduction() {
        val p = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            try {
                val response = apiService.deleteProduction(p.id)
                _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
                if (response.isSuccessful) {
                    loadProductions()
                } else {
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Ошибка удаления производства")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirm = null,
                    snackbarMessage = e.localizedMessage ?: "Ошибка удаления производства"
                )
            }
        }
    }
}
