package com.example.bitpbui1.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitpbui1.data.model.Dish
import com.example.bitpbui1.data.model.DishCategory
import com.example.bitpbui1.data.repository.DishRepository
import com.example.bitpbui1.data.sync.SyncManager
import com.example.bitpbui1.i18n.AppLanguage
import com.example.bitpbui1.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOption {
    DEFAULT,
    PRICE_ASC,
    PRICE_DESC,
    PREP_TIME_ASC
}

class DishViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DishRepository(application)
    private val syncManager = SyncManager(application, repository)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    // Language State (Defaults to EN as required in 4.5)
    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<DishCategory>>(emptyList())
    val categories: StateFlow<List<DishCategory>> = _categories.asStateFlow()

    // All raw dishes from DB
    private val allDishes = MutableStateFlow<List<Dish>>(emptyList())

    // Filtered & Sorted Dishes
    private val _filteredDishes = MutableStateFlow<List<Dish>>(emptyList())
    val filteredDishes: StateFlow<List<Dish>> = _filteredDishes.asStateFlow()

    // Filter & Search Parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _selectedAvailability = MutableStateFlow<Boolean?>(null)
    val selectedAvailability: StateFlow<Boolean?> = _selectedAvailability.asStateFlow()

    private val _minPriceText = MutableStateFlow("")
    val minPriceText: StateFlow<String> = _minPriceText.asStateFlow()

    private val _maxPriceText = MutableStateFlow("")
    val maxPriceText: StateFlow<String> = _maxPriceText.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DEFAULT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // User message feedback
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        refreshData()
        observeNetworkAndSync()
    }

    private fun observeNetworkAndSync() {
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    val result = syncManager.performAutoSync()
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        if (count > 0) {
                            _userMessage.value = "msg_synced"
                        }
                    }
                    refreshData()
                }
            }
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.EN) AppLanguage.VI else AppLanguage.EN
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun refreshData() {
        _categories.value = repository.getAllCategories()
        allDishes.value = repository.getAllDishes()
        applyFiltersAndSort()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFiltersAndSort()
    }

    fun onCategoryFilterChanged(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
        applyFiltersAndSort()
    }

    fun onAvailabilityFilterChanged(isAvailable: Boolean?) {
        _selectedAvailability.value = isAvailable
        applyFiltersAndSort()
    }

    fun onPriceRangeChanged(minText: String, maxText: String) {
        _minPriceText.value = minText
        _maxPriceText.value = maxText
        applyFiltersAndSort()
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
        applyFiltersAndSort()
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategoryId.value = null
        _selectedAvailability.value = null
        _minPriceText.value = ""
        _maxPriceText.value = ""
        _sortOption.value = SortOption.DEFAULT
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        var list = allDishes.value

        // 1. Partial match search by name (4.2)
        val query = _searchQuery.value.trim()
        if (query.isNotEmpty()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) }
        }

        // 2. Filter by category (4.2)
        val catId = _selectedCategoryId.value
        if (catId != null) {
            list = list.filter { it.dishCategoryId == catId }
        }

        // 3. Filter by availability (4.2 & 4.4)
        val avail = _selectedAvailability.value
        if (avail != null) {
            list = list.filter { it.isAvailable == avail }
        }

        // 4. Filter by price range (4.2)
        val minP = _minPriceText.value.toDoubleOrNull()
        val maxP = _maxPriceText.value.toDoubleOrNull()
        if (minP != null) {
            list = list.filter { it.price >= minP }
        }
        if (maxP != null) {
            list = list.filter { it.price <= maxP }
        }

        // 5. Sort options (4.2)
        list = when (_sortOption.value) {
            SortOption.DEFAULT -> list
            SortOption.PRICE_ASC -> list.sortedBy { it.price }
            SortOption.PRICE_DESC -> list.sortedByDescending { it.price }
            SortOption.PREP_TIME_ASC -> list.sortedBy { it.preparationTime }
        }

        _filteredDishes.value = list
    }

    fun getDishById(dishId: Long): Dish? {
        return repository.getDishById(dishId)
    }

    fun getCategoryName(categoryId: Long): String {
        return _categories.value.firstOrNull { it.id == categoryId }?.name ?: "N/A"
    }

    fun saveDish(
        dishId: Long = 0,
        name: String,
        priceText: String,
        prepTimeText: String,
        isAvailable: Boolean,
        description: String,
        categoryId: Long,
        imageUrls: List<String>
    ): Boolean {
        val price = priceText.toDoubleOrNull()
        val prepTime = prepTimeText.toIntOrNull()

        val result = repository.saveDish(
            dishId = dishId,
            name = name,
            price = price,
            preparationTime = prepTime,
            isAvailable = isAvailable,
            description = description,
            categoryId = categoryId,
            imageUrls = imageUrls
        )

        return if (result.isSuccess) {
            _userMessage.value = "msg_dish_saved"
            refreshData()
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message ?: "err_save_failed"
            false
        }
    }

    fun deleteDish(dishId: Long): Boolean {
        val result = repository.deleteDish(dishId)
        return if (result.isSuccess) {
            _userMessage.value = "msg_dish_deleted"
            refreshData()
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message ?: "err_dish_not_found"
            false
        }
    }

    fun addImageToDish(dishId: Long, imageUrl: String): Boolean {
        val result = repository.addImageToDish(dishId, imageUrl)
        return if (result.isSuccess) {
            refreshData()
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message ?: "err_add_image_failed"
            false
        }
    }

    fun setThumbnailImage(dishId: Long, selectedImageId: Long): Boolean {
        val result = repository.setThumbnailImage(dishId, selectedImageId)
        return if (result.isSuccess) {
            _userMessage.value = "msg_thumbnail_updated"
            refreshData()
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message
            false
        }
    }

    fun deleteImage(imageId: Long, dishId: Long): Boolean {
        val result = repository.deleteImage(imageId, dishId)
        return if (result.isSuccess) {
            refreshData()
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message
            false
        }
    }
}
