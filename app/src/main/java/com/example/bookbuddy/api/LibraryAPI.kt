package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.UserList
import retrofit2.Response
import retrofit2.http.*

interface LibraryAPI {
    @GET("/api/library/count/{isbn}")
    suspend fun getLibraryCount(@Path("isbn") isbn: String): Response<Int>

    @GET("/api/library/{isbn}")
    suspend fun getLibrariesBook(@Path("isbn") isbn: String): Response<List<Library>>

    @GET("/api/libraries/{isbn}/{latitude}/{longitude}")
    suspend fun getLibrariesExtendedBook(@Path("isbn") isbn: String, @Path("latitude") latitude: Double, @Path("longitude") longitude: Double): Response<List<LibraryExtended>>
}