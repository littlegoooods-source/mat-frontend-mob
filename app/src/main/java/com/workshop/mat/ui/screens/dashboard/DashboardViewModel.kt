package com.workshop.mat.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.api.TokenManager
import com.workshop.mat.data.model.DashboardDto
import com.workshop.mat.data.model.SalesReportDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class SalesPeriod(val label: String) {
    WEEK("7 дней"),
    MONTH("Месяц"),
    YEAR("Год"),
    ALL("Всё время")
}

data class DashboardUiState(
    val data: DashboardDto? = null,
    val salesData: SalesReportDto? = null,
    val selectedPeriod: SalesPeriod = SalesPeriod.MONTH,
    val isLoading: Boolean = true,
    val isSalesLoading: Boolean = false,
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

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        _uiState.value = _uiState.value.copy(userName = tokenManager.user?.name ?: "")
        loadDashboard()
        loadSalesData()
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

    fun selectPeriod(period: SalesPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadSalesData()
    }

    fun loadSalesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSalesLoading = true)
            try {
                val now = LocalDate.now()
                val dateFrom = when (_uiState.value.selectedPeriod) {
                    SalesPeriod.WEEK -> now.minusDays(7)
                    SalesPeriod.MONTH -> now.minusMonths(1)
                    SalesPeriod.YEAR -> now.minusYears(1)
                    SalesPeriod.ALL -> LocalDate.of(2020, 1, 1)
                }
                val dateTo = now

                val response = apiService.getSalesReport(
                    dateFrom = dateFrom.format(dateFormatter),
                    dateTo = dateTo.format(dateFormatter)
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        salesData = response.body(),
                        isSalesLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSalesLoading = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSalesLoading = false)
            }
        }
    }
}
