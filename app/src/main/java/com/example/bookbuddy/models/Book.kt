package com.example.bookbuddy.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Book(
    val authors: @RawValue List<Author>,
    val bookId: Int,
    val cover: String,
    val description: String,
    val genres: @RawValue List<Genre>,
    val isbn: String,
    val languages: @RawValue List<Language>,
    val pages: Int,
    val publicationDate: String,
    val rating: Double,
    val title: String,
    var cardview: Int = 0
) : Parcelable