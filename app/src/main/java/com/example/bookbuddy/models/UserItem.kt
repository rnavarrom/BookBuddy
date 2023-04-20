package com.example.bookbuddy.models

data class UserItem(
    val bannedUsers: List<Any> = emptyList(),
    val comments: List<Any> = emptyList(),
    val creationDate: Any = "",
    val followUserfolloweds: List<Any> = emptyList(),
    val followUsers: List<Any> = emptyList(),
    val isadmin: Boolean = false,
    val name: String = "",
    val password: String = "",
    val profiles: List<Any> = emptyList(),
    val reports: List<Any> = emptyList(),
    val userId: Int = -1,
    val email: String = ""
)
