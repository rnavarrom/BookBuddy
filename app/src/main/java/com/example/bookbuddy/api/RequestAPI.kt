package com.example.bookbuddy.api

import retrofit2.Response
import retrofit2.http.*

interface RequestAPI {
    @POST("/api/book/request/{isbn}")
    suspend fun insertRequest(@Path("isbn") isbn: String): Response<Boolean>
}