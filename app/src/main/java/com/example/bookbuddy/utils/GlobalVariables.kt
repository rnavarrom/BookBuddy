package com.example.bookbuddy.utils

import android.view.Menu
import androidx.navigation.NavController
import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.User
import com.example.bookbuddy.models.UserItem
import com.google.android.material.navigation.NavigationView
import java.io.File

// Variables used in all the application

lateinit var navView: NavigationView
lateinit var navController: NavController
lateinit var navTop: Menu
var currentUser: User? = null
lateinit var currentProfile: Profile
var currentPicture: File? = null
lateinit var currentUserCreate: UserItem
val keyboardValue: Int = 250
var currentLanguageChanged: Boolean = false

// Check if the profiles has been initialized
fun isProfileInitialized(): Boolean {
    return ::currentProfile.isInitialized
}
