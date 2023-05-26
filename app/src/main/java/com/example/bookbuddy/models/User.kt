package com.example.bookbuddy.models

public data class User(
    val isadmin: Boolean = false,
    var name: String = "",
    var haspicture: Boolean = false,
    val userId: Int = -1
)
