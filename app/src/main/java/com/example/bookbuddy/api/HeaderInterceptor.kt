package com.example.bookbuddy.api

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class HeaderInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .build()
        return chain.proceed(request)
    }
}

var logging = HttpLoggingInterceptor()