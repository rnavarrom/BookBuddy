package com.example.bookbuddy.utils

import androidx.navigation.NavController
import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.models.UserItem
import com.google.android.material.navigation.NavigationView
import java.text.Normalizer
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

//data class DataLists()

lateinit var navView: NavigationView
lateinit var navController : NavController
lateinit var currentUser : User
lateinit var currentProfile: Profile
var currentPicture: File? = null
lateinit var currentUserCreate : UserItem
var dialogValue: String = "125"
val keyboardValue : Int = 250
var currentLanguageChanged: Boolean = false

var dummyValue = 358






//assert("áéíóů".unaccent() == "aeiou")
