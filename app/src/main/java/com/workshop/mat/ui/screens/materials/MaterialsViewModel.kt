package com.workshop.mat.ui.screens.materials

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

data class MaterialsUiState(
    val materials: List<MaterialListItemDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val search: String = "",
    val categoryFilter: String = "",
    val includeArchived: Boolean = false,
    val showDialog: Boolean = false,
    val editingMaterial: MaterialListItemDto? = null,
    val formName: String = "",
    val formUnit: String = "шт",
    val formColor: String = "",
    val formCategory: String = "",
    val formDescription: String = "",
    val formMinimumStock: String = "",
    val isSaving: Boolean = false,
    val showDeleteConfirm: MaterialListItemDto? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MaterialsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialsUiState())
    val uiState: StateFlow<MaterialsUiState> = _uiState.asStateFlow()

    init {
        loadMaterials()
        loadCategories()
    }

    fun updateSearch(value: String) {
        _uiState.value = _uiState.value.copy(search = value)
        loadMaterials()
    }

    fun updateCategoryFilter(value: String) {
        _uiState.value = _uiState.value.copy(categoryFilter = value)
        loadMaterials()
    }

    fun toggleIncludeArchived() {
        _uiState.value = _uiState.value.copy(includeArchived = !_uiState.value.includeArchived)
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val s = _uiState.value
                val response = apiService.getMaterials(
                    search = s.search.ifBlank { null },
                    category = s.categoryFilter.ifBlank { null },
                    includeArchived = s.includeArchived
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        materials = response.body() ?: emptyList(),
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

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getMaterialCategories()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(categories = response.body() ?: emptyList())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = e.localizedMessage ?: "Ошибка загрузки категорий"
                )
            }
        }
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showDialog = true, editingMaterial = null,
            formName = "", formUnit = "шт", formColor = "",
            formCategory = "", formDescription = "", formMinimumStock = ""
        )
    }

    fun openEditDialog(material: MaterialListItemDto) {
        _uiState.value = _uiState.value.copy(
            showDialog = true, editingMaterial = material,
            formName = material.name, formUnit = material.unit,
            formColor = material.color ?: "", formCategory = material.category ?: "",
            formDescription = material.description ?: "",
            formMinimumStock = material.minimumStock?.toString() ?: ""
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false, editingMaterial = null)
    }

    fun updateFormName(v: String) { _uiState.value = _uiState.value.copy(formName = v) }
    fun updateFormUnit(v: String) { _uiState.value = _uiState.value.copy(formUnit = v) }
    fun updateFormColor(v: String) { _uiState.value = _uiState.value.copy(formColor = v) }
    fun updateFormCategory(v: String) { _uiState.value = _uiState.value.copy(formCategory = v) }
    fun updateFormDescription(v: String) { _uiState.value = _uiState.value.copy(formDescription = v) }
    fun updateFormMinimumStock(v: String) { _uiState.value = _uiState.value.copy(formMinimumStock = v) }

    fun saveMaterial() {
        val s = _uiState.value
        if (s.formName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            try {
                val minStock = s.formMinimumStock.toDoubleOrNull()
                val response = if (s.editingMaterial != null) {
                    apiService.updateMaterial(s.editingMaterial.id, MaterialUpdateDto(
                        name = s.formName, unit = s.formUnit,
                        color = s.formColor.ifBlank { null },
                        category = s.formCategory.ifBlank { null },
                        description = s.formDescription.ifBlank { null },
                        minimumStock = minStock
                    ))
                } else {
                    apiService.createMaterial(MaterialCreateDto(
                        name = s.formName, unit = s.formUnit,
                        color = s.formColor.ifBlank { null },
                        category = s.formCategory.ifBlank { null },
                        description = s.formDescription.ifBlank { null },
                        minimumStock = minStock
                    ))
                }
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, showDialog = false)
                    loadMaterials()
                    loadCategories()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        com.google.gson.JsonParser.parseString(errorBody).asJsonObject.get("message")?.asString
                    } catch (_: Exception) { null }
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        snackbarMessage = message ?: "Ошибка сохранения"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    snackbarMessage = e.localizedMessage ?: "Ошибка сохранения"
                )
            }
        }
    }

    fun archiveMaterial(material: MaterialListItemDto) {
        viewModelScope.launch {
            try {
                val response = if (material.isArchived) apiService.unarchiveMaterial(material.id)
                else apiService.archiveMaterial(material.id)
                if (response.isSuccessful) {
                    loadMaterials()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        com.google.gson.JsonParser.parseString(errorBody).asJsonObject.get("message")?.asString
                    } catch (_: Exception) { null }
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = message ?: "Ошибка архивирования"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = e.localizedMessage ?: "Ошибка архивирования"
                )
            }
        }
    }

    fun showDeleteConfirm(material: MaterialListItemDto) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = material)
    }

    fun dismissDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun deleteMaterial() {
        val material = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            try {
                val response = apiService.deleteMaterial(material.id)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
                    loadMaterials()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        com.google.gson.JsonParser.parseString(errorBody).asJsonObject.get("message")?.asString
                    } catch (_: Exception) { null }
                    _uiState.value = _uiState.value.copy(
                        showDeleteConfirm = null,
                        snackbarMessage = message ?: "Невозможно удалить материал: он используется"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirm = null,
                    snackbarMessage = e.localizedMessage ?: "Ошибка удаления"
                )
            }
        }
    }
}
