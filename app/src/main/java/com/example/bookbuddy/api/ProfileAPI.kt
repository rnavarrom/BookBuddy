package com.example.bookbuddy.api

import com.example.bookbuddy.models.Extra.Author
import com.example.bookbuddy.models.Extra.Genre
import com.example.bookbuddy.models.Language
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.Profile
import retrofit2.Response
import retrofit2.http.*

interface ProfileAPI {

    @GET("/api/profile/{userid}")
    suspend fun getProfileUser(@Path("userid") userId: Int): Response<Profile>

    @GET("/api/profile/search/genres/{name}/{position}")
    suspend fun getSearchGenres(
        @Path("name") name: String,
        @Path("position") position: Int
    ): Response<List<Genre>>

    @GET("/api/profile/search/authors/{name}/{position}")
    suspend fun getSearchAuthors(
        @Path("name") name: String,
        @Path("position") position: Int
    ): Response<List<Author>>

    @GET("/api/profile/search/languages/{name}/{position}")
    suspend fun getSearchLanguages(
        @Path("name") name: String,
        @Path("position") position: Int
    ): Response<List<Language>>

    @GET("/api/profile/search/libraries/{name}/{position}")
    suspend fun getSearchLibraries(
        @Path("name") name: String,
        @Path("position") position: Int
    ): Response<List<LibraryExtended>>


    @PUT("/api/profile/genre/{id}/{genre}")
    suspend fun updateProfileGenre(
        @Path("id") id: Int,
        @Path("genre") genre: Int
    ): Response<Boolean>

    @PUT("/api/profile/author/{id}/{author}")
    suspend fun updateProfileAuthor(
        @Path("id") id: Int,
        @Path("author") author: Int
    ): Response<Boolean>
}