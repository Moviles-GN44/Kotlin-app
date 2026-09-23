package com.uniandesfood.data.model

enum class WaitTimeCategory {
    FAST,     // < 5 min (Green)
    MODERATE, // 5-15 min (Amber)
    LONG      // > 15 min (Red)
}

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val buildingTag: String = "",
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
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "buildingTag" to buildingTag,
            "walkDistancesFromBuilding" to walkDistancesFromBuilding,
            "waitTimeCategory" to waitTimeCategory.name,
            "waitTimeLabel" to waitTimeLabel,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "averagePriceCOP" to averagePriceCOP,
            "paymentMethods" to paymentMethods,
            "isVeganFriendly" to isVeganFriendly,
            "isGlutenFreeFriendly" to isGlutenFreeFriendly,
            "isLactoseFreeFriendly" to isLactoseFreeFriendly,
            "menu" to menu.map { it.toMap() }
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): Restaurant {
            val menuList = (map["menu"] as? List<Map<String, Any?>>)?.map { MenuItem.fromMap(it) } ?: emptyList()
            val waitCategory = try {
                WaitTimeCategory.valueOf(map["waitTimeCategory"] as? String ?: "FAST")
            } catch (e: Exception) {
                WaitTimeCategory.FAST
            }
            return Restaurant(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                buildingTag = map["buildingTag"] as? String ?: "",
                walkDistancesFromBuilding = (map["walkDistancesFromBuilding"] as? Map<String, Number>)
                    ?.mapValues { it.value.toInt() } ?: mapOf("ML" to 2, "RGD" to 4, "Franco" to 6, "C" to 5, "W" to 8),
                waitTimeCategory = waitCategory,
                waitTimeLabel = map["waitTimeLabel"] as? String ?: "< 5 MIN WAIT",
                rating = (map["rating"] as? Number)?.toDouble() ?: 4.7,
                reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 128,
                averagePriceCOP = (map["averagePriceCOP"] as? Number)?.toInt() ?: 16000,
                paymentMethods = (map["paymentMethods"] as? List<String>) ?: listOf("Nequi", "Daviplata", "Cards", "Cash"),
                isVeganFriendly = map["isVeganFriendly"] as? Boolean ?: false,
                isGlutenFreeFriendly = map["isGlutenFreeFriendly"] as? Boolean ?: false,
                isLactoseFreeFriendly = map["isLactoseFreeFriendly"] as? Boolean ?: false,
                menu = menuList
            )
        }
    }
}
