package com.workshop.mat.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.model.OperationHistoryItemDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val items: List<OperationHistoryItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val page: Int = 1,
    val totalPages: Int = 1,
    val operationTypeFilter: String = "",
    val entityTypeFilter: String = "",
    val includeCancelled: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { loadHistory() }

    fun updateOperationTypeFilter(v: String) { _uiState.value = _uiState.value.copy(operationTypeFilter = v, page = 1); loadHistory() }
    fun updateEntityTypeFilter(v: String) { _uiState.value = _uiState.value.copy(entityTypeFilter = v, page = 1); loadHistory() }
    fun toggleIncludeCancelled() { _uiState.value = _uiState.value.copy(includeCancelled = !_uiState.value.includeCancelled, page = 1); loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val s = _uiState.value
                val response = apiService.getHistory(
                    operationType = s.operationTypeFilter.ifBlank { null },
                    entityType = s.entityTypeFilter.ifBlank { null },
                    includeCancelled = s.includeCancelled,
                    page = s.page,
                    pageSize = 50
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = _uiState.value.copy(
                        items = body?.items ?: emptyList(),
                        totalPages = body?.totalPages ?: 1,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Ошибка загрузки")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun nextPage() {
        if (_uiState.value.page < _uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(page = _uiState.value.page + 1)
            loadHistory()
        }
    }

    fun prevPage() {
        if (_uiState.value.page > 1) {
            _uiState.value = _uiState.value.copy(page = _uiState.value.page - 1)
            loadHistory()
        }
    }
}
