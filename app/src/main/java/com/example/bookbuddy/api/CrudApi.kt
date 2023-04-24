package com.example.bookbuddy.api

import android.util.Log
import com.example.bookbuddy.Utils.Constants
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Path
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

    suspend fun getUserLogin(userName: String, password: String): UserItem {
        val response = getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password).body()
        return response!!
    }
    suspend fun getUserExists(userName: String): Boolean {
        val response = getRetrofit().create(BookAPI::class.java).getUserExists(userName).body()
        return response!!
    }
    suspend fun getEmailExists(email: String): Boolean {
        val response = getRetrofit().create(BookAPI::class.java).getEmailExists(email).body()
        return response!!
    }

    suspend fun getSimpleSearch(book: String): ArrayList<SimpleBook>{
        val response = getRetrofit().create(BookAPI::class.java).getSimpleSearch(book).body()
        return response!!
    }

    suspend fun addUserToAPI(user: UserItem): Boolean {
        val call = getRetrofit().create(BookAPI::class.java).insertUser(user.name, user.password, user.email)
        return call.isSuccessful
    }

/*
    suspend fun insert(user : UserItem) : Boolean{
        var succesful = false
        // Create Retrofit
        val retrofit = getRetrofit()

        // Create Service
        val service = retrofit.create(BookAPI::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("name", user.name)
        jsonObject.put("password", user.password)
        jsonObject.put("email", user.email)

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            val response = service.insertUser(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    succesful = true
                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.toString() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                        )
                    )

                    Log.d("Pretty Printed JSON :", prettyJson)

                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }


            }


        }
        return succesful
    }
*/

    suspend fun getBook(isbn: String): Book {
        val response = getRetrofit().create(BookAPI::class.java).getBookInfo(isbn).body()
        return response!!
    }

    // Readed
    suspend fun getReadedsFromUser(user_id: Int, position: Int): List<Readed?> {
        val response = getRetrofit().create(ReadedAPI::class.java).getReadedsFromUser(user_id, position).body()
        return response!!
    }

    suspend fun getReadedsFromBook(user_id: Int, book_id: Int): Readed? {
        val response = getRetrofit().create(ReadedAPI::class.java).getReadedsFromBook(user_id, book_id)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun addReadedToAPI(readed: Readed): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).insertReaded(readed)
        return call.isSuccessful
    }

    suspend fun deleteReadedToAPI(readed_id: Int): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).deleteReaded(readed_id)
        return call.isSuccessful
    }

    suspend fun getCommentsFromBook(book_id: Int, position: Int): List<Comment>? {
        val response = getRetrofit().create(CommentAPI::class.java).getCommentsBook(book_id, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getCommentsCounter(book_id: Int): Int? {
        val response = getRetrofit().create(CommentAPI::class.java).getCommentsCounter(book_id)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun addCommentToAPI(commenttext: String, userid: Int, bookid: Int): Boolean {
        val call = getRetrofit().create(CommentAPI::class.java).insertComment(commenttext, userid, bookid)
        return call.isSuccessful
    }

    suspend fun addImageToAPI(image: String): Boolean {
        val call = getRetrofit().create(ImageAPI::class.java).insertImage(image)
        return call.isSuccessful
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