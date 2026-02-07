package com.workshop.mat.ui.screens.products

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

data class RecipeItemForm(
    val materialId: String = "",
    val quantity: String = ""
)

data class ProductsUiState(
    val products: List<ProductListItemDto> = emptyList(),
    val materials: List<MaterialListItemDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val search: String = "",
    val categoryFilter: String = "",
    val includeArchived: Boolean = false,
    // Form
    val showDialog: Boolean = false,
    val editingProduct: ProductResponseDto? = null,
    val formName: String = "",
    val formCategory: String = "",
    val formDescription: String = "",
    val formProductionTime: String = "",
    val formWeight: String = "",
    val formEstimatedCost: String = "",
    val formRecommendedPrice: String = "",
    val formFileLinks: String = "",
    val formMarkupPercent: String = "100",
    val formRecipeItems: List<RecipeItemForm> = emptyList(),
    val isSaving: Boolean = false,
    // Copy
    val showCopyDialog: Boolean = false,
    val productToCopy: ProductListItemDto? = null,
    val copyName: String = "",
    // Delete
    val showDeleteConfirm: ProductListItemDto? = null
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadMaterials()
        loadCategories()
    }

    fun updateSearch(v: String) { _uiState.value = _uiState.value.copy(search = v); loadProducts() }
    fun updateCategoryFilter(v: String) { _uiState.value = _uiState.value.copy(categoryFilter = v); loadProducts() }
    fun toggleIncludeArchived() { _uiState.value = _uiState.value.copy(includeArchived = !_uiState.value.includeArchived); loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val s = _uiState.value
                val response = apiService.getProducts(
                    search = s.search.ifBlank { null },
                    category = s.categoryFilter.ifBlank { null },
                    includeArchived = s.includeArchived
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(products = response.body() ?: emptyList(), isLoading = false)
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
                val response = apiService.getMaterials(includeArchived = false)
                if (response.isSuccessful) _uiState.value = _uiState.value.copy(materials = response.body() ?: emptyList())
            } catch (_: Exception) {}
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getProductCategories()
                if (response.isSuccessful) _uiState.value = _uiState.value.copy(categories = response.body() ?: emptyList())
            } catch (_: Exception) {}
        }
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showDialog = true, editingProduct = null,
            formName = "", formCategory = "", formDescription = "",
            formProductionTime = "", formWeight = "", formEstimatedCost = "",
            formRecommendedPrice = "", formFileLinks = "", formMarkupPercent = "100",
            formRecipeItems = emptyList()
        )
    }

    fun openEditDialog(product: ProductListItemDto) {
        viewModelScope.launch {
            try {
                val response = apiService.getProductById(product.id)
                if (response.isSuccessful) {
                    val p = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        showDialog = true, editingProduct = p,
                        formName = p.name, formCategory = p.category ?: "",
                        formDescription = p.description ?: "",
                        formProductionTime = if (p.productionTimeMinutes > 0) p.productionTimeMinutes.toString() else "",
                        formWeight = if (p.weight > 0) p.weight.toString() else "",
                        formEstimatedCost = p.estimatedCost?.toString() ?: "",
                        formRecommendedPrice = p.recommendedPrice?.toString() ?: "",
                        formFileLinks = p.fileLinks ?: "",
                        formMarkupPercent = p.markupPercent.toString(),
                        formRecipeItems = p.recipeItems.map { RecipeItemForm(it.materialId.toString(), it.quantity.toString()) }
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun closeDialog() { _uiState.value = _uiState.value.copy(showDialog = false, editingProduct = null) }

    fun updateFormName(v: String) { _uiState.value = _uiState.value.copy(formName = v) }
    fun updateFormCategory(v: String) { _uiState.value = _uiState.value.copy(formCategory = v) }
    fun updateFormDescription(v: String) { _uiState.value = _uiState.value.copy(formDescription = v) }
    fun updateFormProductionTime(v: String) { _uiState.value = _uiState.value.copy(formProductionTime = v) }
    fun updateFormEstimatedCost(v: String) { _uiState.value = _uiState.value.copy(formEstimatedCost = v) }
    fun updateFormRecommendedPrice(v: String) { _uiState.value = _uiState.value.copy(formRecommendedPrice = v) }
    fun updateFormFileLinks(v: String) { _uiState.value = _uiState.value.copy(formFileLinks = v) }
    fun updateFormMarkupPercent(v: String) { _uiState.value = _uiState.value.copy(formMarkupPercent = v) }

    fun addRecipeItem() {
        _uiState.value = _uiState.value.copy(
            formRecipeItems = _uiState.value.formRecipeItems + RecipeItemForm()
        )
    }

    fun updateRecipeItem(index: Int, materialId: String? = null, quantity: String? = null) {
        val items = _uiState.value.formRecipeItems.toMutableList()
        if (index < items.size) {
            items[index] = items[index].copy(
                materialId = materialId ?: items[index].materialId,
                quantity = quantity ?: items[index].quantity
            )
            _uiState.value = _uiState.value.copy(formRecipeItems = items)
            recalculateWeight()
        }
    }

    fun removeRecipeItem(index: Int) {
        _uiState.value = _uiState.value.copy(
            formRecipeItems = _uiState.value.formRecipeItems.filterIndexed { i, _ -> i != index }
        )
        recalculateWeight()
    }

    private fun recalculateWeight() {
        val materials = _uiState.value.materials
        var totalKg = 0.0
        _uiState.value.formRecipeItems.forEach { item ->
            val mat = materials.find { it.id.toString() == item.materialId }
            val qty = item.quantity.toDoubleOrNull() ?: 0.0
            if (mat != null && qty > 0) {
                val unit = mat.unit.lowercase().trim()
                when (unit) {
                    "кг", "kg" -> totalKg += qty
                    "г", "g", "гр" -> totalKg += qty / 1000
                }
            }
        }
        _uiState.value = _uiState.value.copy(formWeight = if (totalKg > 0) "%.4f".format(totalKg) else "")
    }

    fun saveProduct() {
        val s = _uiState.value
        if (s.formName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            try {
                val recipeItems = s.formRecipeItems.mapNotNull { item ->
                    val matId = item.materialId.toIntOrNull() ?: return@mapNotNull null
                    val qty = item.quantity.toDoubleOrNull() ?: return@mapNotNull null
                    RecipeItemCreateDto(matId, qty)
                }

                if (s.editingProduct != null) {
                    apiService.updateProduct(s.editingProduct.id, ProductUpdateDto(
                        name = s.formName, category = s.formCategory.ifBlank { null },
                        description = s.formDescription.ifBlank { null },
                        productionTimeMinutes = s.formProductionTime.toIntOrNull() ?: 0,
                        weight = s.formWeight.toDoubleOrNull() ?: 0.0,
                        estimatedCost = s.formEstimatedCost.toDoubleOrNull(),
                        recommendedPrice = s.formRecommendedPrice.toDoubleOrNull(),
                        fileLinks = s.formFileLinks.ifBlank { null },
                        markupPercent = s.formMarkupPercent.toDoubleOrNull() ?: 100.0,
                        recipeItems = recipeItems
                    ))
                } else {
                    apiService.createProduct(ProductCreateDto(
                        name = s.formName, category = s.formCategory.ifBlank { null },
                        description = s.formDescription.ifBlank { null },
                        productionTimeMinutes = s.formProductionTime.toIntOrNull() ?: 0,
                        weight = s.formWeight.toDoubleOrNull() ?: 0.0,
                        estimatedCost = s.formEstimatedCost.toDoubleOrNull(),
                        recommendedPrice = s.formRecommendedPrice.toDoubleOrNull(),
                        fileLinks = s.formFileLinks.ifBlank { null },
                        markupPercent = s.formMarkupPercent.toDoubleOrNull() ?: 100.0,
                        recipeItems = recipeItems
                    ))
                }
                _uiState.value = _uiState.value.copy(isSaving = false, showDialog = false)
                loadProducts()
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }

    fun archiveProduct(product: ProductListItemDto) {
        viewModelScope.launch {
            try {
                if (product.isArchived) apiService.unarchiveProduct(product.id)
                else apiService.archiveProduct(product.id)
                loadProducts()
            } catch (_: Exception) {}
        }
    }

    fun openCopyDialog(product: ProductListItemDto) {
        _uiState.value = _uiState.value.copy(showCopyDialog = true, productToCopy = product, copyName = "${product.name} (копия)")
    }
    fun closeCopyDialog() { _uiState.value = _uiState.value.copy(showCopyDialog = false) }
    fun updateCopyName(v: String) { _uiState.value = _uiState.value.copy(copyName = v) }

    fun copyProduct() {
        val s = _uiState.value
        val product = s.productToCopy ?: return
        if (s.copyName.isBlank()) return
        viewModelScope.launch {
            try {
                apiService.copyProduct(product.id, ProductCopyDto(s.copyName))
                _uiState.value = _uiState.value.copy(showCopyDialog = false)
                loadProducts()
            } catch (_: Exception) {}
        }
    }

    fun showDeleteConfirm(product: ProductListItemDto) { _uiState.value = _uiState.value.copy(showDeleteConfirm = product) }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }

    fun deleteProduct() {
        val product = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            try {
                apiService.deleteProduct(product.id)
                _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
                loadProducts()
            } catch (_: Exception) {}
        }
    }
}
