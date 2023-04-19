package com.example.bookbuddy.api

import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.models.Book
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

class CrudApi(): CoroutineScope {
    private var job: Job = Job()

    //private var okHttpClient: OkHttpClient = UnsafeOkHttpClient.unsafeOkHttpClient

    private var okHttpClient = OkHttpClient.Builder().apply {
        ignoreAllSSLErrors()
    }.addInterceptor(logging).build()

    private fun getRetrofit(): Retrofit {
        val gson = GsonBuilder()
            //reformateja json
            .setLenient()
            .create()

        //okHttpClient = okHttpClient.newBuilder().addInterceptor(logging).build()

        return Retrofit.Builder().baseUrl(Constants.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
    }
    private fun getClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(logging)
            .build()

    suspend fun getUserLogin(userName: String, password: String): Boolean {
        val response = getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password).body()
        return response!!
    }

    suspend fun getBook(isbn: String): Book {
        val response = getRetrofit().create(BookAPI::class.java).getBookInfo(isbn).body()
        return response!!
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

}

fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? = arrayOf()
        override fun checkClientTrusted(
            chain: Array<out java.security.cert.X509Certificate>?,
            authType: String?
        ) = Unit
        override fun checkServerTrusted(
            chain: Array<out java.security.cert.X509Certificate>?,
            authType: String?
        ) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier(HostnameVerifier { _, _ -> true })
    return this
}