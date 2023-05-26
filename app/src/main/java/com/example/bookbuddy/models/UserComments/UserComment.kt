package com.example.bookbuddy.models.UserComments

data class UserComment(
    val bannedUsers: List<Any>,
    val comments: List<Any>,
    val creationDate: Any,
    val email: Any,
    val followUserfolloweds: List<Any>,
    val followUsers: List<Any>,
    val isadmin: Boolean,
    val name: String,
    val password: Any,
    val haspicture: Boolean,
    val profiles: List<Any>,
    val readeds: List<Any>,
    val reports: List<Any>,
    val userId: Int
)