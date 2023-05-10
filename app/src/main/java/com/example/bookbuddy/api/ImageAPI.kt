package com.example.bookbuddy.api

import android.media.Image
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.io.File

interface ImageAPI {

    @GET("/api/images/{userid}")
    suspend fun getImage(@Path("userid") userId: Int): Response<ResponseBody>

    @Multipart
    @POST("/api/images/")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ResponseBody>

    @POST("/api/images/")
    suspend fun insertImage(@Body image: File): Response<File>

}