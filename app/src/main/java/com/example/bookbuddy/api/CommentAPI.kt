package com.example.bookbuddy.api

import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.User.Comment2
import com.example.bookbuddy.models.UserList
import retrofit2.Response
import retrofit2.http.*

interface CommentAPI {

    @GET("/api/comments/book/{book_id}/{position}")
    suspend fun getCommentsBook(@Path("book_id") bookInt: Int, @Path("position") position: Int): Response<List<Comment>>


    @GET("/api/comments/book/{book_id}")
    suspend fun getCommentsCounter(@Path("book_id") bookInt: Int): Response<Int>
    //@POST("/api/comment/{}")
    //suspend fun insertComment(@Body comment: Comment2): Response<Comment2>

    @POST("/api/comment/{commenttext}/{userid}/{bookid}")
    suspend fun insertComment(@Path("commenttext") commenttext: String, @Path("userid") userid: Int, @Path("bookid") bookid: Int ): Response<Comment>

}