package com.example.bookbuddy.api

import com.example.bookbuddy.models.*
import retrofit2.Response
import retrofit2.http.*

interface LibraryAPI {
    @GET("/api/library/count/{isbn}")
    suspend fun getLibraryCount(@Path("isbn") isbn: String): Response<Int>

    /*
    @GET("/api/library/{isbn}")
    suspend fun getLibrariesBook(@Path("isbn") isbn: String): Response<List<Library>>
    */
    @GET("/api/libraries/{isbn}")
    suspend fun getLibrariesBook(@Path("isbn") isbn: String): Response<List<LibraryExtended>>

    @GET("/api/libraries/{isbn}/{latitude}/{longitude}")
    suspend fun getLibrariesExtendedBook(
        @Path("isbn") isbn: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<List<LibraryExtended>>

    @GET("/api/library/{name}/{search}/{position}")
    suspend fun getLibraries(
        @Path("name") libraryName: String,
        @Path("search") search: Boolean,
        @Path("position") position: Int
    ): Response<List<Library>>

    @GET("/api/library/{library_id}/{position}")
    suspend fun getLibraryBooks(
        @Path("library_id") libraryId: Int,
        @Path("position") position: Int
    ): Response<List<Book>>

    @POST("/api/library/{name}/{lat}/{lon}/{zip}")
    suspend fun insertLibrary(
        @Path("name") libraryName: String,
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double,
        @Path("zip") zip: String
    ): Response<Boolean>

    @PUT("/api/library/{id}/{name}/{lat}/{lon}/{zip}")
    suspend fun updateLibrary(
        @Path("id") libraryId: Int,
        @Path("name") libraryName: String,
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double,
        @Path("zip") zip: String
    ): Response<Boolean>

    @DELETE("/api/library/{id}")
    suspend fun deleteLibrary(@Path("id") libraryId: Int): Response<Boolean>
}