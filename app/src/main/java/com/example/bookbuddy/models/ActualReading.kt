package com.example.bookbuddy.models

data class ActualReading(
    val authors: Any,
    val bookId: Int,
    val cover: String,
    val description: Any,
    val genres: Any,
    val isbn: String,
    val languages: Any,
    val pages: Int,
    var pagesReaded: Int,
    val publicationDate: Any,
    val rating: Double,
    val title: Any,
    val readedId: Int
)