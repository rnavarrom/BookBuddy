package com.example.bookbuddy.models

import android.os.Bundle

data class Library(
    val lat: Double,
    val libraryId: Int,
    val lon: Double,
    val name: String,
    val zipCode: String
) : java.io.Serializable

data class LibraryExtended(
    val copies: Int,
    val distance: Double?,
    val library: Library,
    var cardview: Int
) : java.io.Serializable

