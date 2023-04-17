package com.example.bookbuddy.models

data class UserItem(
    val bannedUsers: List<Any>,
    val comments: List<Any>,
    val creationDate: Any,
    val followUserfolloweds: List<Any>,
    val followUsers: List<Any>,
    val isadmin: Boolean,
    val name: String,
    val password: String,
    val profiles: List<Any>,
    val reports: List<Any>,
    val userId: Int,
    val email: String
)