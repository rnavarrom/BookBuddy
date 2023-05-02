package com.example.bookbuddy.models

data class Library(
    val lat: Double,
    val libraryId: Int,
    val lon: Double,
    val name: String,
    val zipCode: String
) : java.io.Serializable

data class LibraryExtended(
    val copies: Int,
    val distance: Double,
    val library: Library
) : java.io.Serializable