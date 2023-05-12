package com.example.bookbuddy.models

data class Profile(
    val author: Author?,
    var authorId: Int?,
    val genre: Genre?,
    var genreId: Int?,
    val profileId: Int,
    val user: Any?,
    val userId: Int
)