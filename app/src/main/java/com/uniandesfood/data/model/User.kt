package com.uniandesfood.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val preferredBuilding: String = "ML"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "preferredBuilding" to preferredBuilding
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                preferredBuilding = map["preferredBuilding"] as? String ?: "ML"
            )
        }
    }
}
