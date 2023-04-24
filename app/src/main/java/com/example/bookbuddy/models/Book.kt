package com.example.bookbuddy.models

data class Book(
    val authors: List<Author>,
    val bookId: Int,
    val cover: String,
    val description: String,
    val pages: Int,
    val genres: List<Genre>,
    val isbn: String,
    val languages: List<Language>,
    val publicationDate: String,
    val rating: Int,
    val title: String
)