package com.example.bookbuddy.api

import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.User.Comment
import retrofit2.Response
import retrofit2.http.*

interface ProfileAPI {

    @GET("/api/profile/{userid}")
    suspend fun getProfileUser(@Path("userid") userId: Int): Response<Profile>

    //@PUT("/api/comment/{commentid}/{commenttext}/{rating}/{userid}/{bookid}")
    //suspend fun updateProfile(@Path("commentid") commentid: Int, @Path("commenttext") commenttext: String, @Path("rating") rating: Int, @Path("userid") userid: Int, @Path("bookid") bookid: Int ): Response<Comment>

    @POST("/api/profile/{genreid}/{authorid}/{userid}")
    suspend fun insertProfile(@Path("genreid") genreId: Int, @Path("authorid") authorId: Int, @Path("userid") userId: Int): Response<Profile>

    @DELETE("/api/profile/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Profile>


}