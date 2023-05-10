package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.models.UserList
import retrofit2.Response
import retrofit2.http.*

interface BookAPI {
    @GET("/users/")
    suspend fun getUsers(): Response<UserList>
    @GET("/api/user/name/{userName}")
    suspend fun getUserExists(@Path("userName") userName: String): Response<Boolean>
    @GET("/api/user/email/{email}")
    suspend fun getEmailExists(@Path("email") email: String): Response<Boolean>
    @GET("/api/user/{userName}/{password}")
    suspend fun getUserLogin(@Path("userName") userName: String, @Path("password") password: String): Response<User>
    @GET("/api/user/{userId}")
    suspend fun getUserId(@Path("userId") userId: Int): Response<User>
    @GET("/api/books/search/{book}")
    suspend fun getSimpleSearch(@Path("book") book: String): Response<ArrayList<SimpleBook>>
    @POST("/api/user/{name}/{password}/{email}")
    suspend fun insertUser(@Path("name") name: String, @Path("password") password: String, @Path("email") email: String): Response<Boolean>
    @GET("/api/book/isbn/{isbn}")
    suspend fun getBookInfo(@Path("isbn") isbn: String): Response<Book>
    /*
  @PUT("/api/readed/put/{readedId}/{pagesReaded}")
  suspend fun updatePagesReaded(@Path("readedId") readedId: Int, @Path("pagesReaded") pagesReaded: Int): Response<Boolean>

  @PUT("/api/readed/remove/{readedId}")
  suspend fun removeBookAcount(@Path("readedId") readedId: Int): Response<Boolean>

  @PUT("/api/readed/pending/{readedId}")
  suspend fun setBookPending(@Path("readedId") readedId: Int): Response<Boolean>
  @PUT("/api/readed/read/{readedId}")
  suspend fun setBookRead(@Path("readedId") readedId: Int): Response<Boolean>
  @PUT("/api/readed/reading/{readedId}")
  suspend fun setBookReading(@Path("readedId") readedId: Int): Response<Boolean>

  @POST("/api/readed/postnew/{}")


 */
/*
    @GET("/productes/?llistat")
    suspend fun getProductes():Response<Productes>

    @GET("/productes/")
    suspend fun getProducte(@Query("codi") codi: Int): Response<Productes>

    @GET("/productes/")
    suspend fun getTipus(@Query("tipus") tipus: Int): Response<Productes>

    @POST("/productes/")
    suspend fun insertProducte(@Body producte: Producte): Response<ResponseModel>

    @PUT("/productes/")
    suspend fun updateProducte(@Body producte: Producte): Response<ResponseModel>

    @DELETE("/productes/")
    suspend fun deleteProducte(@Query("codi") codi: Int): Response<ResponseModel>

     */
}