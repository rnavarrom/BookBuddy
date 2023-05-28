package com.example.bookbuddy.api

import com.example.bookbuddy.models.*
import retrofit2.Response
import retrofit2.http.*

interface UserAPI {
    @GET("/api/user/name/{userName}")
    suspend fun getUserExists(@Path("userName") userName: String): Response<Boolean>


    @GET("/api/user/email/{email}")
    suspend fun getEmailExists(@Path("email") email: String): Response<Boolean>

    @GET("/api/user/{userName}/{password}")
    suspend fun getUserLogin(
        @Path("userName") userName: String,
        @Path("password") password: String
    ): Response<User>

    @GET("/api/user/{userId}")
    suspend fun getUserId(@Path("userId") userId: Int): Response<User>

    @POST("/api/user/{name}/{password}/{email}")
    suspend fun insertUser(
        @Path("name") name: String,
        @Path("password") password: String,
        @Path("email") email: String
    ): Response<Boolean>


    @PUT("/api/user/pic/{id}")
    suspend fun updateProfilePic(@Path("id") userId: Int): Response<Boolean>

    @PUT("/api/user/password/{email}/{password}")
    suspend fun updateUserPasswordMail(
        @Path("email") email: String,
        @Path("password") password: String
    ): Response<Boolean>

    @PUT("/api/user/password/{id}/{password}")
    suspend fun updateUserPasswordId(
        @Path("id") userId: Int,
        @Path("password") password: String
    ): Response<Boolean>

    @PUT("/api/user/name/{id}/{name}")
    suspend fun updateUserName(@Path("id") id: Int, @Path("name") name: String): Response<Boolean>

    @GET("/api/user/requests/{position}")
    suspend fun getRequests(@Path("position") position: Int): Response<List<BookRequest>>

    @DELETE("/api/user/request/{id}")
    suspend fun deleteRequest(@Path("id") id: Int): Response<Boolean>

}