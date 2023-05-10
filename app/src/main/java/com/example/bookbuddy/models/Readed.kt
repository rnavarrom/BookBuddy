package com.example.bookbuddy.models

data class Readed(
    val book: Book?,
    val bookId: Int?,
    //val curreading: Boolean?,
    val curreading: Int?,
    val percentatgeRead: Double?,
    val readedId: Int?,
    val user: Any?,
    val userId: Int?
)