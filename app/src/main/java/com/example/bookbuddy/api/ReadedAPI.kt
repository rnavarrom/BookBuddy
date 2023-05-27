package com.example.bookbuddy.api

import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.ActualReading
import com.example.bookbuddy.models.Pending
import retrofit2.Response
import retrofit2.http.*

interface ReadedAPI {
    @GET("/api/readeds/user/{user_id}/{position}")
    suspend fun getReadedsFromUser(@Path("user_id") user_id: Int, @Path("position") position: Int): Response<List<Readed>>
    //suspend fun getUserLogin(@Path("userName") user_id: String, @Path("password") password: String): Response<Boolean>

    @GET("/api/readed/{book_id}/{user_id}")
    suspend fun getReadedsFromBook(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Readed>
    @GET("/api/readed/read/{user_id}/{position}")
    suspend fun getReadBooksFromUser(@Path("user_id") user_id: Int, @Path("position") position: Int): Response<List<Pending>>
    @GET("/api/readed/pending/{user_id}/{position}")
    suspend fun getPendingBooksFromUser(@Path("user_id") user_id: Int, @Path("position") position: Int): Response<List<Pending>>
    @GET("/api/readed/reading/{user_id}/{position}")
    suspend fun getReadingBooksFromUser(@Path("user_id") user_id: Int, @Path("position") position: Int): Response<List<ActualReading>>
    @GET("/api/readed/pendingFilter/{user_id}/{filter}/{position}")
    suspend fun filterPendingBooksFromUser(@Path("user_id") user_id: Int,@Path("filter") filter: String, @Path("position") position: Int): Response<List<Pending>>
    @GET("/api/readed/readFilter/{user_id}/{filter}/{position}")
    suspend fun filterReadBooksFromUser(@Path("user_id") user_id: Int,@Path("filter") filter: String, @Path("position") position: Int): Response<List<Pending>>

    @POST("/api/readed/{}")
    suspend fun insertReaded(@Body readed: Readed): Response<Readed>

    //@PUT("/api/readed/{readed}")
    //suspend fun updateProducte(@Body readed: Readed): Response<Readed>

    @DELETE("/api/readed/{id}")
    suspend fun deleteReaded(@Path("id") id: Int): Response<Readed>
    @PUT("/api/readed/put/{readedId}/{pagesReaded}")
    suspend fun updatePagesReaded(@Path("readedId") readedId: Int, @Path("pagesReaded") pagesReaded: Int): Response<Boolean>
    //@PUT("/api/readed/remove/{book_id}/{user_id}")
    //suspend fun removeBookReading(@Path("readedId") readedId: Int): Response<Boolean>
    @DELETE("/api/readed/delete/{book_id}/{user_id}")
    suspend fun removeBookReading(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Boolean>
    @PUT("/api/readed/pending/{book_id}/{user_id}")
    suspend fun setBookPending(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Boolean>
    @PUT("/api/readed/read/{book_id}/{user_id}")
    suspend fun setBookRead(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Boolean>
    @PUT("/api/readed/reading/{book_id}/{user_id}")
    suspend fun setBookReading(@Path("book_id") book_id: Int, @Path("user_id") user_id: Int): Response<Boolean>

    //@POST("/api/readed/postnew/{userId}/{bookId}")
    //suspend fun postBookReading(@Path("userId") userId: Int, @Path("bookId") bookId: Int): Response<Boolean>
}