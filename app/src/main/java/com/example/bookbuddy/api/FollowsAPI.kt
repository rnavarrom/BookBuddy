package com.example.bookbuddy.api

import com.example.bookbuddy.models.*
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

interface FollowsAPI {


    @GET("/api/follows/{userid}/{userfollowid}")
    suspend fun getFollowing(
        @Path("userid") userid: Int,
        @Path("userfollowid") userfollowid: Int
    ): Response<Boolean>

    @GET("/api/follows/count/{userid}")
    suspend fun getFollowersUser(@Path("userid") userId: Int): Response<Int>

    @GET("/api/follows/getcontacts/{userid}/{position}")
    suspend fun getFollowersProfile(
        @Path("userid") userId: Int,
        @Path("position") position: Int
    ): Response<List<UserItem>>

    @GET("/api/follows/contacts/{userid}")
    suspend fun getEmailsContact(
        @Path("userid") userId: Int,
        @Query("emails") emails: List<String>
    ): Response<Int>

    @POST("/api/follows/{userid}/{userfollowid}")
    suspend fun insertFollow(
        @Path("userid") userid: Int,
        @Path("userfollowid") userfollowid: Int
    ): Response<Boolean>

    @DELETE("/api/follows/{userid}/{userfollowid}")
    suspend fun deleteFollow(
        @Path("userid") userid: Int,
        @Path("userfollowid") userfollowid: Int
    ): Response<Boolean>


}