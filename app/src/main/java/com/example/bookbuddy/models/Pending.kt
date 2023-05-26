package com.example.bookbuddy.models

data class Pending(
    val bookId: Int,
    val bookLibraries: List<Any>,
    val comments: List<Any>,
    val cover: String,
    val description: String,
    val isbn: String,
    val pages: Int,
    val publicationDate: String,
    val rating: Double,
    val readeds: List<Any>,
    val title: String
)