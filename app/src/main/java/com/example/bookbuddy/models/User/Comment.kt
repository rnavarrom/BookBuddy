package com.example.bookbuddy.models.User

data class Comment(
    val book: Any? = null,
    var bookId: Int,
    val comentId: Int? = null,
    var comentText: String,
    val fecha: String? = null,
    val user: User? = null,
    var userId: Int
)

data class Comment2(
    var bookId: Int,
    val comentId: Int? = null,
    var comentText: String,
    val fecha: String? = null,
    var userId: Int
)