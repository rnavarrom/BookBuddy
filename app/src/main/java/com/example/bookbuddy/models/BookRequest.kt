package com.example.bookbuddy.models

data class BookRequest(
    val bookIsbn: String,
    val bookRequest1: Int,
    var cardview: Int = 0
)