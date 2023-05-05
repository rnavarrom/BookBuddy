package com.example.bookbuddy.models.Test

data class User(
    val actualReading: List<ActualReading>,
    val isadmin: Boolean,
    var name: String,
    val pending: List<Pending>,
    val profilePicture: Any,
    val readed: List<Pending>,
    val userId: Int
)