package com.example.bookbuddy.utils

import androidx.navigation.NavController
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.models.UserItem
import com.google.android.material.navigation.NavigationView
import java.text.Normalizer

//data class DataLists()

lateinit var navView: NavigationView
lateinit var navController : NavController
lateinit var currentUser : User
lateinit var currentUserCreate : UserItem
var dialogValue: String = "125"






//assert("áéíóů".unaccent() == "aeiou")
