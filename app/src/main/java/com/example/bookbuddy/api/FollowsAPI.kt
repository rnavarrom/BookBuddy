package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Follows
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.User.Comment2
import com.example.bookbuddy.models.UserList
import retrofit2.Response
import retrofit2.http.*

interface FollowsAPI {


    @GET("/api/follows/{userid}/{userfollowid}")
    suspend fun getFollowing(@Path("userid") userid: Int, @Path("userfollowid") userfollowid: Int): Response<Boolean>

    @GET("/api/follows/count/{userid}")
    suspend fun getFollowersUser(@Path("userid") userId: Int): Response<Int>

    @POST("/api/follows/{userid}/{userfollowid}")
    suspend fun insertFollow(@Path("userid") userid: Int, @Path("userfollowid") userfollowid: Int): Response<Boolean>

    @DELETE("/api/follows/{userid}/{userfollowid}")
    suspend fun deleteFollow(@Path("userid") userid: Int, @Path("userfollowid") userfollowid: Int): Response<Follows>


}