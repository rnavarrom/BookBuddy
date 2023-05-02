package com.example.bookbuddy.api

import retrofit2.Response
import retrofit2.http.*

interface ImageAPI {

    //@GET("/api/comments/book/{book_id}/{position}")
    //suspend fun getCommentsBook(@Path("book_id") bookInt: Int, @Path("position") position: Int): Response<List<Comment>>

    @POST("/api/images/{image}")
    suspend fun insertImage(@Path("image") image: String): Response<Boolean>

}