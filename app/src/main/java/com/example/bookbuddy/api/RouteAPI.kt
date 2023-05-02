package com.example.bookbuddy.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteAPI {
    @GET("/v2/directions/foot-walking/")
    suspend fun getWalkingRoute(
        @Query("api_key") apikey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): Response<com.example.bookbuddy.models.Response>

    @GET("/v2/directions/driving-car/")
    suspend fun getCarRoute(
        @Query("api_key") apikey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): Response<com.example.bookbuddy.models.Response>
}