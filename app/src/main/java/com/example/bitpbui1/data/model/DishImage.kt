package com.example.bitpbui1.data.model

data class DishImage(
    val id: Long = 0,
    val dishId: Long = 0,
    val imageUrl: String,
    val isThumbnail: Boolean = false
)
