package com.workshop.mat.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.api.TokenManager
import com.workshop.mat.data.model.DashboardDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val data: DashboardDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(userName = tokenManager.user?.name ?: "")
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getDashboard()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        data = response.body(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось загрузить данные"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Ошибка подключения"
                )
            }
        }
    }
}
