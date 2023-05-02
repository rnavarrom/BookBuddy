package com.example.bookbuddy.models

import java.time.LocalDate

data class UserItem(
    val bannedUsers: List<Any> = emptyList(),
    val comments: List<Any> = emptyList(),
    val creationDate: LocalDate? = null,
    val followUserfolloweds: List<Any> = emptyList(),
    val followUsers: List<Any> = emptyList(),
    val isadmin: Boolean = false,
    var name: String = "",
    var password: String = "",
    val profiles: List<Any> = emptyList(),
    val reports: List<Any> = emptyList(),
    var userId: Int = -1,
    var email: String = "",
    var pending: List<Any> = emptyList(),
    var readed: List<Any> = emptyList(),
    var reading: Book? = null
)
