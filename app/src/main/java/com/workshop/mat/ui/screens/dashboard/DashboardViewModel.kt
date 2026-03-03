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

data class SalesTimePoint(
    val label: String,
    val salesCount: Int = 0,
    val revenue: Double = 0.0
)

data class DashboardUiState(
    val data: DashboardDto? = null,
    val salesData: SalesReportDto? = null,
    val salesTimeSeries: List<SalesTimePoint> = emptyList(),
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

                val reportResponse = apiService.getSalesReport(
                    dateFrom = dateFrom.format(dateFormatter),
                    dateTo = now.format(dateFormatter)
                )
                val salesReport = if (reportResponse.isSuccessful) reportResponse.body() else null

                val soldResponse = apiService.getFinishedProducts(status = "Sold")
                val timeSeries = if (soldResponse.isSuccessful) {
                    val items = soldResponse.body() ?: emptyList()
                    buildTimeSeries(items, dateFrom, now, _uiState.value.selectedPeriod)
                } else emptyList()

                _uiState.value = _uiState.value.copy(
                    salesData = salesReport,
                    salesTimeSeries = timeSeries,
                    isSalesLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSalesLoading = false)
            }
        }
    }

    private fun buildTimeSeries(
        items: List<com.workshop.mat.data.model.FinishedProductListItemDto>,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        period: SalesPeriod
    ): List<SalesTimePoint> {
        val salesByDate = mutableMapOf<LocalDate, Int>()
        val revenueByDate = mutableMapOf<LocalDate, Double>()

        items.forEach { item ->
            val dateStr = item.soldAt ?: item.saleDate ?: return@forEach
            val soldDate = try {
                LocalDate.parse(dateStr.take(10))
            } catch (_: Exception) { return@forEach }

            if (!soldDate.isBefore(dateFrom) && !soldDate.isAfter(dateTo)) {
                salesByDate[soldDate] = (salesByDate[soldDate] ?: 0) + 1
                revenueByDate[soldDate] = (revenueByDate[soldDate] ?: 0.0) + (item.sellPrice ?: 0.0)
            }
        }

        val dayFormatter = DateTimeFormatter.ofPattern("dd.MM")
        val monthFormatter = DateTimeFormatter.ofPattern("MMM")

        return when (period) {
            SalesPeriod.WEEK -> {
                (0..6).map { i ->
                    val d = dateFrom.plusDays(i.toLong() + 1)
                    SalesTimePoint(
                        label = d.format(dayFormatter),
                        salesCount = salesByDate[d] ?: 0,
                        revenue = revenueByDate[d] ?: 0.0
                    )
                }
            }
            SalesPeriod.MONTH -> {
                val points = mutableListOf<SalesTimePoint>()
                var cursor = dateFrom.plusDays(1)
                while (!cursor.isAfter(dateTo)) {
                    val weekEnd = cursor.plusDays(6).let { if (it.isAfter(dateTo)) dateTo else it }
                    var count = 0
                    var rev = 0.0
                    var d = cursor
                    while (!d.isAfter(weekEnd)) {
                        count += salesByDate[d] ?: 0
                        rev += revenueByDate[d] ?: 0.0
                        d = d.plusDays(1)
                    }
                    points.add(SalesTimePoint(
                        label = "${cursor.format(dayFormatter)}-${weekEnd.format(dayFormatter)}",
                        salesCount = count,
                        revenue = rev
                    ))
                    cursor = weekEnd.plusDays(1)
                }
                points
            }
            SalesPeriod.YEAR -> {
                (0..11).map { i ->
                    val monthStart = dateTo.minusMonths(11L - i).withDayOfMonth(1)
                    val monthEnd = monthStart.plusMonths(1).minusDays(1)
                    var count = 0
                    var rev = 0.0
                    var d = monthStart
                    while (!d.isAfter(monthEnd) && !d.isAfter(dateTo)) {
                        if (!d.isBefore(dateFrom)) {
                            count += salesByDate[d] ?: 0
                            rev += revenueByDate[d] ?: 0.0
                        }
                        d = d.plusDays(1)
                    }
                    SalesTimePoint(
                        label = monthStart.format(monthFormatter),
                        salesCount = count,
                        revenue = rev
                    )
                }
            }
            SalesPeriod.ALL -> {
                val minYear = if (salesByDate.isEmpty()) dateTo.year else salesByDate.keys.minOf { it.year }
                val maxYear = dateTo.year
                (minYear..maxYear).map { year ->
                    var count = 0
                    var rev = 0.0
                    salesByDate.forEach { (d, c) ->
                        if (d.year == year) {
                            count += c
                            rev += revenueByDate[d] ?: 0.0
                        }
                    }
                    SalesTimePoint(
                        label = year.toString(),
                        salesCount = count,
                        revenue = rev
                    )
                }
            }
        }
    }
}
