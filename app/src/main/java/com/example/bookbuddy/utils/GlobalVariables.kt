package com.example.bookbuddy.utils

import androidx.navigation.NavController
import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.User
import com.example.bookbuddy.models.UserItem
import com.google.android.material.navigation.NavigationView
import java.io.File


lateinit var navView: NavigationView
lateinit var navController : NavController
var currentUser : User? = null
lateinit var currentProfile: Profile
var currentPicture: File? = null
lateinit var currentUserCreate : UserItem
val keyboardValue : Int = 250
var currentLanguageChanged: Boolean = false

fun isProfileInitialized(): Boolean {
    return ::currentProfile.isInitialized
}
