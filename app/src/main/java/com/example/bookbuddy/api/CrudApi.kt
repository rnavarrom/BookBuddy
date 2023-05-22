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
import retrofit2.http.Path
import retrofit2.http.Query
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
/*
    suspend fun getUserLogin(userName: String, password: String): User? {
        val response = getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password).body()
        return response!!
    }

 */


    suspend fun getUserLogin(userName: String, password: String, errorMessage: String): User? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getUserLogin(userName, password) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getUserId(userId: Int, errorMessage: String): User? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getUserId(userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(BookAPI::class.java).getUserId(userId).body()
        //return response!!
    }
    suspend fun getUserExists(userName: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getUserExists(userName) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }
    suspend fun getEmailExists(email: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getEmailExists(email) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getSimpleSearch(position: Int, searchvalues: List<String>, errorMessage: String): ArrayList<SimpleBook>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getSimpleSearch(position, searchvalues) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(BookAPI::class.java).getSimpleSearch(position, searchvalues).body()
        //return response!!
    }

    suspend fun addUserToAPI(user: UserItem, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertUser(user.name, user.password, user.email) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateProfilePic(id: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).updateProfilePic(id) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateUserName(id: Int, name: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).updateUserName(id, name) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(BookAPI::class.java).updateUserName(id, name)
        //return call.isSuccessful
    }


    suspend fun getBookExist(isbn: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookExist(isbn) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        /*
        val response = getRetrofit().create(BookAPI::class.java).getBookExist(isbn)
        if (response.isSuccessful){
            return true
        }
        return false

         */
    }

    suspend fun getBook(isbn: String, userId: Int, errorMessage: String): Book? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookInfo(isbn, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        /*
        val response = getRetrofit().create(BookAPI::class.java).getBookInfo(isbn, userId)
        if (response.isSuccessful){
            return response.body()
        }
        return null

         */
    }

    suspend fun getRecommendedBooks(userId: Int, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getRecommendedBooks(userId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(BookAPI::class.java).getRecommendedBooks(userId, position)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }

    // Books CRUD

    suspend fun getAllBooks(position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBooks(position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getAllBooksSearch(title: String, search: Boolean, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBooksAdminSearch(title, search, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getAllBooksByAuthor(authorId: Int, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBooksByAuthor(authorId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertBook(isbn: String, title: String, description: String, pages: Int,
                           date: String, cover: String, errorMessage: String): Book? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertBook(isbn, title, description, pages, date, cover) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateBook(bookId: Int, isbn: String, title: String, description: String, rating: Double, pages: Int,
                           date: String, cover: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).updateBook(bookId, isbn, title, description, rating, pages, date, cover) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteBook(isbn: String, force: Boolean, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteBook(isbn, force) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // Genres

    suspend fun getGenres(genreName: String, search: Boolean, position: Int, errorMessage: String): List<Genre>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(GenreAPI::class.java).getGenres(genreName, search, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getGenreBooks(genreId: Int, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(GenreAPI::class.java).getGenreBooks(genreId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertGenre(genreName: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(GenreAPI::class.java).insertGenre(genreName) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateGenre(genreId: Int, genreName: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(GenreAPI::class.java).updateGenre(genreId, genreName) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteGenre(genreId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(GenreAPI::class.java).deleteGenre(genreId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // Authors

    suspend fun getAuthors(authorName: String, search: Boolean, position: Int, errorMessage: String): List<Author>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(AuthorAPI::class.java).getAuthors(authorName, search, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getAuthorsBooks(authorId: Int, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(AuthorAPI::class.java).getAuthorBooks(authorId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertAuthor(authorName: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(AuthorAPI::class.java).insertAuthor(authorName) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateAuthor(authorId: Int, authorName: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(AuthorAPI::class.java).updateAuthor(authorId, authorName) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteAuthor(authorId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(AuthorAPI::class.java).deleteAuthor(authorId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // Library
    suspend fun getLibraries(libraryName: String, search: Boolean, position: Int, errorMessage: String): List<Library>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).getLibraries(libraryName, search, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getLibraryBooks(libraryId: Int, position: Int, errorMessage: String): List<Book>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).getLibraryBooks(libraryId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertLibrary(libraryName: String, latitude: Double, longitude: Double, zipCode: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).insertLibrary(libraryName, latitude, longitude, zipCode) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateLibrary(libraryId: Int, libraryName: String, latitude: Double, longitude: Double, zipCode: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).updateLibrary(libraryId, libraryName, latitude, longitude, zipCode) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteLibrary(libraryId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).deleteLibrary(libraryId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // BookAuthors
    suspend fun getBookAuthors(bookId: Int, errorMessage: String): Author? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookAuthor(bookId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertBookAuthors(bookId: Int, authorId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertBookAuthor(bookId, authorId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteBookAuthors(bookId: Int, authorId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteBookAuthor(bookId, authorId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getBookLang(bookId: Int, errorMessage: String): Language? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookLang(bookId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertBookLang(bookId: Int, langId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertBookLang(bookId, langId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteBookLang(bookId: Int, langId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteBookLang(bookId, langId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // BookGenres

    suspend fun getBookGenres(bookId: Int, errorMessage: String): List<Genre>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookGenres(bookId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertBookGenre(bookId: Int, genreId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertBookGenre(bookId, genreId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteBookGenre(bookId: Int, genreId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteBookGenre(bookId, genreId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // BookLibraries
    suspend fun getBookLibraries(bookId: Int, errorMessage: String): List<LibraryExtended>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getBookLibrary(bookId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun insertBookLibrary(bookId: Int, libraryId: Int, copies: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).insertBookLibrary(bookId, libraryId, copies) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun updateBookLibrary(bookId: Int, libraryId: Int, copies: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).updateBookLibrary(bookId, libraryId, copies) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteBookLibrary(bookId: Int, libraryId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteBookLibrary(bookId, libraryId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    // Readed
    suspend fun getReadedsFromUser(user_id: Int, position: Int, errorMessage: String): List<Readed>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).getReadedsFromUser(user_id, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(ReadedAPI::class.java).getReadedsFromUser(user_id, position).body()
        //return response!!
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

    suspend fun getReadedsFromBook(bookId: Int, userId: Int, errorMessage: String): Readed? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).getReadedsFromBook(bookId, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        /*
        val response = getRetrofit().create(ReadedAPI::class.java).getReadedsFromBook(bookId, userId)
        if (response.isSuccessful){
            return response.body()
        }
        return null

         */
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

    suspend fun getCommentsFromBook(book_id: Int, position: Int, errorMessage: String): List<Comment>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).getCommentsBook(book_id, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
/*
        val response = getRetrofit().create(CommentAPI::class.java).getCommentsBook(book_id, position)
        if (response.isSuccessful){
            return response.body()
        }
        return null

 */
    }

    suspend fun getCommentsCounter(book_id: Int, errorMessage: String): Int? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).getCommentsCounter(book_id) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        /*
        val response = getRetrofit().create(CommentAPI::class.java).getCommentsCounter(book_id)
        if (response.isSuccessful){
            return response.body()
        }
        return null

         */
    }

    suspend fun getUserComments(user_id: Int, position: Int, errorMessage: String): List<Comment>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).getUserComments(user_id, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(CommentAPI::class.java).getUserComments(user_id, position)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }


    suspend fun getCommentsFromUser(user_id: Int, book_id: Int, errorMessage: String): Comment? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).getUserComment(user_id, book_id) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        /*
        val response = getRetrofit().create(CommentAPI::class.java).getUserComment(user_id, book_id)
        if (response.isSuccessful){
            return response.body()
        }
        return null

         */
    }


    suspend fun addCommentToAPI(commenttext: String, rating: Int, userid: Int, bookid: Int): Boolean {
        val call = getRetrofit().create(CommentAPI::class.java).insertComment(commenttext, rating, userid, bookid)
        return call.isSuccessful
    }

    suspend fun updateCommentToAPI(commentid: Int, commenttext: String, rating: Int, userid: Int, bookid: Int, errorMessage: String): Comment? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).updateComment(commentid, commenttext, rating, userid, bookid) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(CommentAPI::class.java).updateComment(commentid, commenttext, rating, userid, bookid)
        //return call.isSuccessful
    }

    suspend fun deleteCommentToAPI(id: Int, errorMessage: String): Any? {
        return safeApiCall(
            apiCall = { getRetrofit().create(CommentAPI::class.java).deleteComment(id) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(CommentAPI::class.java).deleteComment(id)
        //return call.isSuccessful
    }

    // Book Requests
    suspend fun addRequestAPI(isbn: String, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(RequestAPI::class.java).insertRequest(isbn) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(RequestAPI::class.java).insertRequest(isbn)
        //return call.body()!!
    }

    suspend fun getRequests(position: Int, errorMessage: String): List<BookRequest>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).getRequests(position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun deleteRequest(id: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(BookAPI::class.java).deleteRequest(id) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }
    // Books
    // Profiles

    suspend fun getProfileUser(userId: Int, errorMessage: String): Profile? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ProfileAPI::class.java).getProfileUser(userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
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

    suspend fun getSearchLanguages(name: String, position: Int, errorMessage: String): List<Language>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ProfileAPI::class.java).getSearchLanguages(name, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun getSearchLibraries(name: String, position: Int, errorMessage: String): List<LibraryExtended>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ProfileAPI::class.java).getSearchLibraries(name, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun addProfileToAPI(genreId: Int, authorId: Int, userId: Int): Profile? {
        val call = getRetrofit().create(ProfileAPI::class.java).insertProfile(genreId, authorId, userId)
        return call.body()
    }

    suspend fun updateProfileGenreToAPI(id: Int, genre: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ProfileAPI::class.java).updateProfileGenre(id, genre) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ProfileAPI::class.java).updateProfileGenre(id, genre)
        //return call.body()
    }

    suspend fun updateProfileAuthorToAPI(id: Int, author: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ProfileAPI::class.java).updateProfileAuthor(id, author) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ProfileAPI::class.java).updateProfileAuthor(id, author)
        //return call.body()
    }

    suspend fun deleteProfileToAPI(id: Int): Boolean{
        val call = getRetrofit().create(ProfileAPI::class.java).deleteProfile(id)
        return call.isSuccessful
    }

    // Images

    suspend fun getUserImage(userId: Int, errorMessage: String): ResponseBody? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ImageAPI::class.java).getImage(userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ImageAPI::class.java).getImage(userId)
        //return call
    }

    suspend fun uploadImageToAPI(isCover: Boolean, image: MultipartBody.Part, errorMessage: String): ResponseBody? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ImageAPI::class.java).uploadImage(isCover, image) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ImageAPI::class.java).uploadImage(image)
        //return call
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
    /*
    suspend fun getBookLibraries(isbn: String): List<Library> {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibrariesBook(isbn).body()
        return response!!
    }
    */
    suspend fun getBookLibraries(isbn: String): List<LibraryExtended>? {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibrariesBook(isbn).body()
        return response
    }

    suspend fun getBookLibrariesExtended(isbn: String, latitude: Double, longitude: Double): List<LibraryExtended>? {
        val response = getRetrofit().create(LibraryAPI::class.java).getLibrariesExtendedBook(isbn, latitude, longitude).body()
        return response
    }

    suspend fun getBookLibrariesCount(isbn: String, errorMessage: String): Int? {
        return safeApiCall(
            apiCall = { getRetrofit().create(LibraryAPI::class.java).getLibraryCount(isbn) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(LibraryAPI::class.java).getLibraryCount(isbn).body()
        //return response!!
    }

    // Follows

    suspend fun getIsFollowing(userId: Int, userFollowedId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).getFollowing(userId, userFollowedId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(FollowsAPI::class.java).getFollowing(userId, userFollowedId)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }

    suspend fun getFollowerCount(userId: Int, errorMessage: String): Int? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).getFollowersUser(userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(FollowsAPI::class.java).getFollowersUser(userId)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }

    suspend fun getFollowersProfile(userId: Int, position: Int, errorMessage: String): List<UserItem>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).getFollowersProfile(userId, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(FollowsAPI::class.java).getFollowersProfile(userId, position)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }

    suspend fun getEmailsContact(userId: Int, emails: List<String>, errorMessage: String): Int? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).getEmailsContact(userId, emails) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val response = getRetrofit().create(FollowsAPI::class.java).getEmailsContact(userId, emails)
        //if (response.isSuccessful){
        //    return response.body()
        //}
        //return null
    }

    suspend fun addFollowToAPI(userId: Int, userFollowedId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).insertFollow(userId, userFollowedId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(FollowsAPI::class.java).insertFollow(userId, userFollowedId)
        //return call.isSuccessful
    }

    suspend fun deleteFollowAPI(userId: Int, userFollowedId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(FollowsAPI::class.java).deleteFollow(userId, userFollowedId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(FollowsAPI::class.java).deleteFollow(userId, userFollowedId)
        //return call.isSuccessful
    }
    suspend fun updateReadedToAPI(readedId: Int, pagesReaded: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).updatePagesReaded(readedId, pagesReaded) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ReadedAPI::class.java).updatePagesReaded(readedId, pagesReaded)
        //return call.isSuccessful
    }
    suspend fun removeBookReading(bookId: Int, userId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).removeBookReading(bookId, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ReadedAPI::class.java).removeBookReading(bookId, userId)
        //return call.isSuccessful
    }
    suspend fun setBookPending(bookId: Int, userId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).setBookPending(bookId, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ReadedAPI::class.java).setBookPending(bookId, userId)
        //return call.isSuccessful
    }
    suspend fun setBookReading(bookId: Int, userId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).setBookReading(bookId, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ReadedAPI::class.java).setBookReading(bookId, userId)
        //return call.isSuccessful
    }
    suspend fun setBookRead(bookId: Int, userId: Int, errorMessage: String): Boolean? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).setBookRead(bookId, userId) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
        //val call = getRetrofit().create(ReadedAPI::class.java).setBookRead(bookId, userId)
        //return call.isSuccessful
    }

    suspend fun filterPendingBook(user_id: Int, filter: String, position: Int, errorMessage: String): List<Pending>? {
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).filterPendingBooksFromUser(user_id, filter, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
    }

    suspend fun filterReadBook(user_id: Int, filter: String, position: Int, errorMessage: String): List<Pending>?{
        return safeApiCall(
            apiCall = { getRetrofit().create(ReadedAPI::class.java).filterReadBooksFromUser(user_id, filter, position) },
            errorListener = errorListener!!,
            errorMessage = errorMessage
        )
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