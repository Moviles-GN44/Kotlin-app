package com.uniandesfood.data.model

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val priceCOP: Int,
    val formattedPrice: String,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isLactoseFree: Boolean = false,
    val photoUrl: String = ""
)
