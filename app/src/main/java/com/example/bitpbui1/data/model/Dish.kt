package com.example.bitpbui1.data.model

data class Dish(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val preparationTime: Int, // Minute(s)
    val isAvailable: Boolean = true,
    val description: String = "",
    val dishCategoryId: Long,
    val images: List<DishImage> = emptyList()
) {
    val isQuickPreparation: Boolean
        get() = preparationTime <= 15

    val thumbnailImage: DishImage?
        get() = images.firstOrNull { it.isThumbnail } ?: images.firstOrNull()
}
