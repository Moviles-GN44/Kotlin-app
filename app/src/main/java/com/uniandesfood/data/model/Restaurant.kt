package com.uniandesfood.data.model

enum class WaitTimeCategory {
    FAST,     // < 5 min (Green)
    MODERATE, // 5-15 min (Amber)
    LONG      // > 15 min (Red)
}

data class Restaurant(
    val id: String,
    val name: String,
    val buildingTag: String,
    val walkDistancesFromBuilding: Map<String, Int> = mapOf("ML" to 2, "RGD" to 4, "Franco" to 6, "C" to 5, "W" to 8),
    val waitTimeCategory: WaitTimeCategory = WaitTimeCategory.FAST,
    val waitTimeLabel: String = "< 5 MIN WAIT",
    val rating: Double = 4.7,
    val reviewCount: Int = 128,
    val averagePriceCOP: Int = 16000,
    val paymentMethods: List<String> = listOf("Nequi", "Daviplata", "Cards", "Cash"),
    val isVeganFriendly: Boolean = true,
    val isGlutenFreeFriendly: Boolean = true,
    val isLactoseFreeFriendly: Boolean = false,
    val menu: List<MenuItem> = emptyList()
)
