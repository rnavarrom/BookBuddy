package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.UserList
import retrofit2.Response
import retrofit2.http.*

interface ReadedAPI {
    @GET("/api/readeds/user/{user_id}/{position}")
    suspend fun getReadedsFromUser(@Path("user_id") user_id: Int, @Path("position") position: Int): Response<List<Readed>>
    //suspend fun getUserLogin(@Path("userName") user_id: String, @Path("password") password: String): Response<Boolean>

    @GET("/api/readed/{book_id}/{user_id}")
    suspend fun getReadedsFromBook(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Readed>

    @POST("/api/readed/{}")
    suspend fun insertReaded(@Body readed: Readed): Response<Readed>

    //@PUT("/api/readed/{readed}")
    //suspend fun updateProducte(@Body readed: Readed): Response<Readed>

    @DELETE("/api/readed/{id}")
    suspend fun deleteReaded(@Path("id") id: Int): Response<Readed>
}