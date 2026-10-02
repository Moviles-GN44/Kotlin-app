package com.uniandesfood.data.model

data class Review(
    val id: String = "",
    val restaurantId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val uid: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "restaurantId" to restaurantId,
        "rating" to rating,
        "comment" to comment,
        "uid" to uid,
        "timestamp" to timestamp
    )
}