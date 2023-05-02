package com.example.bookbuddy.models

data class Follows(
    val code: Int,
    val userFollowedId: Int,
    val user: Any?,
    val userId: Int
)