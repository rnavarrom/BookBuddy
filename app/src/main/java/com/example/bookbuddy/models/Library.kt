package com.example.bookbuddy.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
data class Library(
    val lat: Double,
    val libraryId: Int,
    val lon: Double,
    val name: String,
    val zipCode: String
) : java.io.Serializable
*/
@Parcelize
data class Library(
    val lat: Double,
    val libraryId: Int,
    val lon: Double,
    var name: String,
    val zipCode: String,
    var cardview: Int = 0
) : Parcelable

@Parcelize
data class LibraryExtended(
    var copies: Int,
    val distance: Double?,
    val library: Library,
    var cardview: Int = 0
) : Parcelable

