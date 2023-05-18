package com.example.bookbuddy.models.Test

data class User(
    val actualReading: List<ActualReading> = emptyList(),
    val isadmin: Boolean = false,
    var name: String = "",
    val pending: List<Pending> = emptyList(),
    val haspicture: Boolean = false,
    val readed: List<Pending> = emptyList(),
    val userId: Int = -1
)