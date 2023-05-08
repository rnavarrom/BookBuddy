package com.example.bookbuddy.api

import android.media.Image
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.models.*
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.UserItem
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

class CrudApi(): CoroutineScope {
    val urlapi = "https://api.openrouteservice.org/"
    val apikey = "5b3ce3597851110001cf6248a7e5128d424e4a4dbc75aaece5822482"

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

    private fun getRetrofitRoute(): Retrofit {
        return Retrofit.Builder().baseUrl(urlapi)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    private fun getClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(logging)
            .build()

    suspend fun getUserLogin(userName: String, password: String): User {
        val response = getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password).body()
        return response!!
    }
    suspend fun getUserId(userId: Int): User {
        val response = getRetrofit().create(BookAPI::class.java).getUserId(userId).body()
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

    // Comments

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

    suspend fun getUserComments(user_id: Int, position: Int): List<Comment>? {
        val response = getRetrofit().create(CommentAPI::class.java).getUserComments(user_id, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }


    suspend fun getCommentsFromUser(user_id: Int, book_id: Int): Comment? {
        val response = getRetrofit().create(CommentAPI::class.java).getUserComment(user_id, book_id)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }


    suspend fun addCommentToAPI(commenttext: String, rating: Int, userid: Int, bookid: Int): Boolean {
        val call = getRetrofit().create(CommentAPI::class.java).insertComment(commenttext, rating, userid, bookid)
        return call.isSuccessful
    }

    suspend fun updateCommentToAPI(commentid: Int, commenttext: String, rating: Int, userid: Int, bookid: Int): Boolean {
        val call = getRetrofit().create(CommentAPI::class.java).updateComment(commentid, commenttext, rating, userid, bookid)
        return call.isSuccessful
    }

    suspend fun deleteCommentToAPI(id: Int): Boolean{
        val call = getRetrofit().create(CommentAPI::class.java).deleteComment(id)
        return call.isSuccessful
    }

    // Images

    suspend fun getImageToAPI(image: String): String? {
        val call = getRetrofit().create(ImageAPI::class.java).getImage(image)
        return call.body()
    }

    suspend fun addImageToAPI(image: File): File? {
        val call = getRetrofit().create(ImageAPI::class.java).insertImage(image)
        return call.body()
    }

    suspend fun uploadImageToAPI(image: MultipartBody.Part): Response<ResponseBody> {
        val call = getRetrofit().create(ImageAPI::class.java).uploadImage(image)
        return call
    }

    // Libraries

    suspend fun getBookLibraries(isbn: String): List<Library> {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibrariesBook(isbn).body()
        return response!!
    }

    suspend fun getBookLibrariesExtended(isbn: String, latitude: Double, longitude: Double): List<LibraryExtended> {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibrariesExtendedBook(isbn, latitude, longitude).body()
        return response!!
    }

    suspend fun getBookLibrariesCount(isbn: String): Int {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibraryCount(isbn).body()
        return response!!
    }

    // Follows

    suspend fun getIsFollowing(userId: Int, userFollowedId: Int): Boolean? {
        val response = getRetrofit().create(FollowsAPI::class.java).getFollowing(userId, userFollowedId)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getFollowerCount(userId: Int): Int? {
        println("BB")
        val response = getRetrofit().create(FollowsAPI::class.java).getFollowersUser(userId)
        println("AA")
        if (response.isSuccessful){
            println("AAA" + response.body().toString())
            return response.body()
        }
        return null
    }

    suspend fun addFollowToAPI(userId: Int, userFollowedId: Int): Boolean {
        val call = getRetrofit().create(FollowsAPI::class.java).insertFollow(userId, userFollowedId)
        return call.isSuccessful
    }

    suspend fun deleteFollowAPI(userId: Int, userFollowedId: Int): Boolean{
        val call = getRetrofit().create(FollowsAPI::class.java).deleteFollow(userId, userFollowedId)
        return call.isSuccessful
    }

    suspend fun updateReadedToAPI(readedId: Int, pagesReaded: Int): Boolean {
        val call = getRetrofit().create(BookAPI::class.java).updatePagesReaded(readedId, pagesReaded)
        return call.isSuccessful
    }

    suspend fun removeBookReading(readedId: Int): Boolean {
        val call = getRetrofit().create(BookAPI::class.java).removeBookReading(readedId)
        return call.isSuccessful
    }

    // Trace routes
    suspend fun getWalkingRoute(start: String, end: String): CleanResponse? {
        var response: Response<com.example.bookbuddy.models.Response>? = null

        val corrutina = launch {
            response =
                getRetrofitRoute().create(RouteAPI::class.java)
                    .getWalkingRoute(apikey, start, end)
        }
        corrutina.join()

        if (response!!.isSuccessful) {
            val resposta = CleanResponse(
                response!!.body()!!.features[0].geometry.coordinates,
                response!!.body()!!.features[0].properties.summary.distance,
                response!!.body()!!.features[0].properties.summary.duration
            )

            return resposta
        } else{
            return null
        }
    }

    suspend fun getCarRoute(start: String, end: String): CleanResponse? {
        var response: Response<com.example.bookbuddy.models.Response>? = null

        val corrutina = launch {
            response =
                getRetrofitRoute().create(RouteAPI::class.java)
                    .getCarRoute(apikey, start, end)
        }
        corrutina.join()

        if (response!!.isSuccessful) {
            if (response!!.isSuccessful) {
                val resposta = CleanResponse(
                    response!!.body()!!.features[0].geometry.coordinates,
                    response!!.body()!!.features[0].properties.summary.distance,
                    response!!.body()!!.features[0].properties.summary.duration
                )

                return resposta
            } else{
                return null
            }
        } else{
            return null
        }
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