package com.uniandesfood.data.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val priceCOP: Int = 0,
    val formattedPrice: String = "",
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isLactoseFree: Boolean = false,
    val photoUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "priceCOP" to priceCOP,
            "formattedPrice" to formattedPrice,
            "isVegan" to isVegan,
            "isGlutenFree" to isGlutenFree,
            "isLactoseFree" to isLactoseFree,
            "photoUrl" to photoUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): MenuItem {
            return MenuItem(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                priceCOP = (map["priceCOP"] as? Number)?.toInt() ?: 0,
                formattedPrice = map["formattedPrice"] as? String ?: "",
                isVegan = map["isVegan"] as? Boolean ?: false,
                isGlutenFree = map["isGlutenFree"] as? Boolean ?: false,
                isLactoseFree = map["isLactoseFree"] as? Boolean ?: false,
                photoUrl = map["photoUrl"] as? String ?: ""
            )
        }
    }
}
