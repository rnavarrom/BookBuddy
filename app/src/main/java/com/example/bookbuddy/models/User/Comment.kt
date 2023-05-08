package com.example.bookbuddy.models.User

import com.example.bookbuddy.models.Book

data class Comment(
    val book: Book? = null,
    var bookId: Int,
    var rating: Int,
    val comentId: Int? = null,
    var comentText: String,
    val fecha: String? = null,
    val user: User? = null,
    var userId: Int,
    var typeCardview: Int = 0
)

data class Comment2(
    var bookId: Int,
    val comentId: Int? = null,
    var comentText: String,
    val fecha: String? = null,
    var userId: Int
)