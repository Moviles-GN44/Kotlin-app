package com.uniandesfood.data.model

data class User(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val preferredBuilding: String = "ML"
)
