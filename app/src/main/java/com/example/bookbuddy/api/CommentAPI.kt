package com.example.bookbuddy.api

import com.example.bookbuddy.models.User.Comment
import retrofit2.Response
import retrofit2.http.*

interface CommentAPI {

    @GET("/api/comments/book/{book_id}/{position}")
    suspend fun getCommentsBook(@Path("book_id") bookInt: Int, @Path("position") position: Int): Response<List<Comment>>


    @GET("/api/comments/book/{book_id}")
    suspend fun getCommentsCounter(@Path("book_id") bookInt: Int): Response<Int>

    @GET("/api/comments/user/{user_id}/{position}")
    suspend fun getUserComments(@Path("user_id") userId: Int, @Path("position") position: Int): Response<List<Comment>>

    @GET("/api/comment/{userId}/{bookId}")
    suspend fun getUserComment(@Path("userId") userId: Int, @Path("bookId") bookId: Int): Response<Comment>

    //@POST("/api/comment/{}")
    //suspend fun insertComment(@Body comment: Comment2): Response<Comment2>

    @PUT("/api/comment/{commentid}/{commenttext}/{rating}/{userid}/{bookid}")
    suspend fun updateComment(@Path("commentid") commentid: Int, @Path("commenttext") commenttext: String, @Path("rating") rating: Int, @Path("userid") userid: Int, @Path("bookid") bookid: Int ): Response<Boolean>

    @POST("/api/comment/{commenttext}/{rating}/{userid}/{bookid}")
    suspend fun insertComment(@Path("commenttext") commenttext: String, @Path("rating") rating: Int, @Path("userid") userid: Int, @Path("bookid") bookid: Int ): Response<Boolean>

    @DELETE("/api/comment/{id}")
    suspend fun deleteComment(@Path("id") id: Int): Response<Boolean>


}