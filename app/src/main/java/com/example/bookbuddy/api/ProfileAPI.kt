package com.example.bookbuddy.api

import com.example.bookbuddy.models.Language
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.Test.Author
import com.example.bookbuddy.models.Test.Genre
import com.example.bookbuddy.models.User.Comment
import retrofit2.Response
import retrofit2.http.*

interface ProfileAPI {

    @GET("/api/profile/{userid}")
    suspend fun getProfileUser(@Path("userid") userId: Int): Response<Profile>

    //@PUT("/api/comment/{commentid}/{commenttext}/{rating}/{userid}/{bookid}")
    //suspend fun updateProfile(@Path("commentid") commentid: Int, @Path("commenttext") commenttext: String, @Path("rating") rating: Int, @Path("userid") userid: Int, @Path("bookid") bookid: Int ): Response<Comment>

    @GET("/api/profile/search/genres/{name}/{position}")
    suspend fun getSearchGenres(@Path("name") name: String, @Path("position") position: Int): Response<List<Genre>>

    @GET("/api/profile/search/authors/{name}/{position}")
    suspend fun getSearchAuthors(@Path("name") name: String, @Path("position") position: Int): Response<List<Author>>

    @GET("/api/profile/search/languages/{name}/{position}")
    suspend fun getSearchLanguages(@Path("name") name: String, @Path("position") position: Int): Response<List<Language>>

    @GET("/api/profile/search/libraries/{name}/{position}")
    suspend fun getSearchLibraries(@Path("name") name: String, @Path("position") position: Int): Response<List<LibraryExtended>>

    @POST("/api/profile/{genreid}/{authorid}/{userid}")
    suspend fun insertProfile(@Path("genreid") genreId: Int, @Path("authorid") authorId: Int, @Path("userid") userId: Int): Response<Profile>

    @PUT("/api/profile/genre/{id}/{genre}")
    suspend fun updateProfileGenre(@Path("id") id: Int, @Path("genre") genre: Int): Response<Boolean>

    @PUT("/api/profile/author/{id}/{author}")
    suspend fun updateProfileAuthor(@Path("id") id: Int, @Path("author") author: Int): Response<Boolean>

    @DELETE("/api/profile/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Profile>


}