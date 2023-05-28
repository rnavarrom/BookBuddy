package com.example.bookbuddy.api

import com.example.bookbuddy.models.Author
import com.example.bookbuddy.models.Book
import retrofit2.Response
import retrofit2.http.*

interface AuthorAPI {
    @GET("/api/author/{name}/{search}/{position}")
    suspend fun getAuthors(
        @Path("name") authorName: String,
        @Path("search") search: Boolean,
        @Path("position") position: Int
    ): Response<List<Author>>

    @GET("/api/author/{author_id}/{position}")
    suspend fun getAuthorBooks(
        @Path("author_id") authorId: Int,
        @Path("position") position: Int
    ): Response<List<Book>>

    @POST("/api/author/{name}")
    suspend fun insertAuthor(@Path("name") authorName: String): Response<Boolean>

    @PUT("/api/author/{id}/{name}")
    suspend fun updateAuthor(
        @Path("id") authorId: Int,
        @Path("name") authorName: String
    ): Response<Boolean>

    @DELETE("/api/author/{id}")
    suspend fun deleteAuthor(@Path("id") authorId: Int): Response<Boolean>
}