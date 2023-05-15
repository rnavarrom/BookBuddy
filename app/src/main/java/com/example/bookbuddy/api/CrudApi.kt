package com.example.bookbuddy.api

import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.Test.User
import com.example.bookbuddy.models.*
import com.example.bookbuddy.models.Test.ActualReading
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.base.safeApiCall
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
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

class CrudApi(private val errorListener: ApiErrorListener? = null): CoroutineScope {
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

    suspend fun getUserLogin(userName: String, password: String): User? {
        val response = getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password).body()
        return response!!
    }

    /*
    suspend fun getUserLogin(userName: String, password: String, errorMessage: String): User? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }
    */

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

    suspend fun getSimpleSearch(position: Int, searchvalues: List<String>): ArrayList<SimpleBook>{
        val response = getRetrofit().create(BookAPI::class.java).getSimpleSearch(position, searchvalues).body()
        return response!!
    }

    suspend fun addUserToAPI(user: UserItem): Boolean {
        val call = getRetrofit().create(BookAPI::class.java).insertUser(user.name, user.password, user.email)
        return call.isSuccessful
    }

    suspend fun updateUserName(id: Int, name: String): Boolean {
        val call = getRetrofit().create(BookAPI::class.java).updateUserName(id, name)
        return call.isSuccessful
    }



    suspend fun getBook(isbn: String): Book {
        val response = getRetrofit().create(BookAPI::class.java).getBookInfo(isbn).body()
        return response!!
    }

    suspend fun getRecommendedBooks(userId: Int, position: Int): List<Book>? {
        val response = getRetrofit().create(BookAPI::class.java).getRecommendedBooks(userId, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    // Readed
    suspend fun getReadedsFromUser(user_id: Int, position: Int): List<Readed?> {
        val response = getRetrofit().create(ReadedAPI::class.java).getReadedsFromUser(user_id, position).body()
        return response!!
    }

    suspend fun getReadBooksFromUser(user_id: Int, position: Int): List<Pending?> {
        val response = getRetrofit().create(ReadedAPI::class.java).getReadBooksFromUser(user_id, position).body()
        return response!!
    }
    suspend fun getPendingBooksFromUser(user_id: Int, position: Int): List<Pending?> {
        val response = getRetrofit().create(ReadedAPI::class.java).getPendingBooksFromUser(user_id, position).body()
        return response!!
    }
    suspend fun getReadingBooksFromUser(user_id: Int, position: Int): List<ActualReading?> {
        val response = getRetrofit().create(ReadedAPI::class.java).getReadingBooksFromUser(user_id, position).body()
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

    suspend fun updatePagesReaded(readed_id: Int): Boolean {
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

    // Profiles

    suspend fun getProfileUser(userId: Int): Profile? {
        val response = getRetrofit().create(ProfileAPI::class.java).getProfileUser(userId)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getSearchGenres(name: String, position: Int): List<com.example.bookbuddy.models.Test.Genre>? {
        val response = getRetrofit().create(ProfileAPI::class.java).getSearchGenres(name, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getSearchAuthors(name: String, position: Int): List<com.example.bookbuddy.models.Test.Author>? {
        val response = getRetrofit().create(ProfileAPI::class.java).getSearchAuthors(name, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }


    suspend fun addProfileToAPI(genreId: Int, authorId: Int, userId: Int): Profile? {
        val call = getRetrofit().create(ProfileAPI::class.java).insertProfile(genreId, authorId, userId)
        return call.body()
    }

    suspend fun updateProfileGenreToAPI(id: Int, genre: Int): Boolean? {
        val call = getRetrofit().create(ProfileAPI::class.java).updateProfileGenre(id, genre)
        return call.body()
    }

    suspend fun updateProfileAuthorToAPI(id: Int, author: Int): Boolean? {
        val call = getRetrofit().create(ProfileAPI::class.java).updateProfileAuthor(id, author)
        return call.body()
    }

    suspend fun deleteProfileToAPI(id: Int): Boolean{
        val call = getRetrofit().create(ProfileAPI::class.java).deleteProfile(id)
        return call.isSuccessful
    }

    // Images

    suspend fun getUserImage(userId: Int): Response<ResponseBody> {
        val call = getRetrofit().create(ImageAPI::class.java).getImage(userId)
        return call
    }

    suspend fun uploadImageToAPI(image: MultipartBody.Part): Response<ResponseBody> {
        val call = getRetrofit().create(ImageAPI::class.java).uploadImage(image)
        return call
    }
    /*
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
    */
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
        val response = getRetrofit().create(FollowsAPI::class.java).getFollowersUser(userId)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getFollowersProfile(userId: Int, position: Int): List<UserItem>? {
        val response = getRetrofit().create(FollowsAPI::class.java).getFollowersProfile(userId, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null
    }

    suspend fun getEmailsContact(userId: Int, emails: List<String>): Int? {
        val response = getRetrofit().create(FollowsAPI::class.java).getEmailsContact(userId, emails)
        if (response.isSuccessful){
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
        val call = getRetrofit().create(ReadedAPI::class.java).updatePagesReaded(readedId, pagesReaded)
        return call.isSuccessful
    }
    suspend fun removeBookReading(bookId: Int, userId: Int): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).removeBookReading(bookId, userId)
        return call.isSuccessful
    }
    suspend fun setBookPending(bookId: Int, userId: Int): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).setBookPending(bookId, userId)
        return call.isSuccessful
    }
    suspend fun setBookReading(bookId: Int, userId: Int): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).setBookReading(bookId, userId)
        return call.isSuccessful
    }
    suspend fun setBookRead(bookId: Int, userId: Int): Boolean {
        val call = getRetrofit().create(ReadedAPI::class.java).setBookRead(bookId, userId)
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