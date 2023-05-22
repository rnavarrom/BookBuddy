package com.example.bookbuddy.models
/*
data class Author(
    val authorId: Int,
    var name: String,
    val pseudonym: Any
)
*/

data class Author(
    val authorId: Int,
    var name: String,
    val pseudonym: String = "",
    var cardview: Int = 0
)
