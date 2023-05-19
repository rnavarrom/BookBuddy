package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.models.User.Comment
import retrofit2.Response
import retrofit2.http.*

interface GenreAPI {
    @GET("/api/genre/{name}/{search}/{position}")
    suspend fun getGenres(@Path("name") genreName: String, @Path("search") search: Boolean, @Path("position") position: Int): Response<List<Genre>>

    @GET("/api/genre/{genre_id}/{position}")
    suspend fun getGenreBooks(@Path("genre_id") genreId: Int, @Path("position") position: Int): Response<List<Book>>

    @POST("/api/genre/{name}")
    suspend fun insertGenre(@Path("name") genreName: String): Response<Boolean>

    @PUT("/api/genre/{id}/{name}")
    suspend fun updateGenre(@Path("id") genreId: Int, @Path("name") genreName: String): Response<Boolean>

    @DELETE("/api/genre/{id}")
    suspend fun deleteGenre(@Path("id") genreId: Int): Response<Boolean>
}