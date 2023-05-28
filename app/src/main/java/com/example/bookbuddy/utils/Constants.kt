package com.example.bookbuddy.Utils

import com.bumptech.glide.request.RequestOptions
import com.example.bookbuddy.R
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class Constants {
    companion object{
        //const val BASE_URL = "https://172.16.24.120:7137/"
        const val BASE_URL = "https://192.168.1.55:7137/"
        //const val BASE_URL = "https://192.168.1.123:7137/"
        //const val BASE_URL = "https://172.16.24.198:7137/"
        //const val BASE_URL = "https://172.16.24.136:7137/"
        //const val BASE_URL = "https://192.168.1.58:7137/"

        const val ErrrorMessage = "Can't reach the server. Try again!"

        val bookRequestOptions = RequestOptions()
            .placeholder(R.drawable.book_placeholder)
            .error(R.drawable.book_placeholder)
            //.centerInside()
            //.centerCrop()
            .fitCenter()

        val profileRequestOptions = RequestOptions()
            .placeholder(R.drawable.default_picture) // ID del recurso del placeholder
            .error(R.drawable.default_picture)
    }
}