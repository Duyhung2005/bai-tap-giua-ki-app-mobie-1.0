package com.example.bitpbui1.data.validation

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val messageKey: String) : ValidationResult()
}

object DishValidation {
    fun validateDish(
        name: String,
        price: Double?,
        preparationTime: Int?,
        categoryId: Long,
        existingCategories: List<Long>,
        isDuplicateNameInCategory: Boolean
    ): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("err_name_empty")
        }
        if (price == null || price <= 0) {
            return ValidationResult.Error("err_price_invalid")
        }
        if (preparationTime == null || preparationTime <= 0) {
            return ValidationResult.Error("err_prep_time_invalid")
        }
        if (categoryId <= 0 || !existingCategories.contains(categoryId)) {
            return ValidationResult.Error("err_category_invalid")
        }
        if (isDuplicateNameInCategory) {
            return ValidationResult.Error("err_name_duplicate")
        }
        return ValidationResult.Success
    }
}
