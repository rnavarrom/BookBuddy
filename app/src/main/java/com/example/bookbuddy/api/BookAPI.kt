package com.example.bookbuddy.api

import com.example.bookbuddy.models.*
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

interface BookAPI {

    @GET("/api/books/recommended/{userid}/{position}")
    suspend fun getRecommendedBooks(
        @Path("userid") userid: Int,
        @Path("position") position: Int
    ): Response<List<Book>>


    @GET("/api/book/exist/{isbn}")
    suspend fun getBookExist(@Path("isbn") isbn: String): Response<Boolean>

    @GET("/api/book/isbn/{isbn}/{userid}")
    suspend fun getBookInfo(@Path("isbn") isbn: String, @Path("userid") userid: Int): Response<Book>

    @GET("/api/books/search/{position}")
    suspend fun getSimpleSearch(
        @Path("position") position: Int,
        @Query("searchdata") searchdata: List<String>
    ): Response<ArrayList<SimpleBook>>


    @GET("/api/books/{position}")
    suspend fun getBooks(@Path("position") position: Int): Response<List<Book>>

    @GET("/api/books/author/{authorid}/{position}")
    suspend fun getBooksByAuthor(
        @Path("authorid") authorid: Int,
        @Path("position") position: Int
    ): Response<List<Book>>

    @GET("/api/books/{title}/{search}/{position}")
    suspend fun getBooksAdminSearch(
        @Path("title") title: String,
        @Path("search") search: Boolean,
        @Path("position") position: Int
    ): Response<List<Book>>

    @POST("/api/book/{isbn}/{title}/{description}/{pages}/{date}")
    suspend fun insertBook(
        @Path("isbn") isbn: String, @Path("title") title: String,
        @Path("description") description: String, @Path("pages") pages: Int,
        @Path("date") date: String, @Query("cover") cover: String
    ): Response<Book>

    @PUT("/api/book/{id}/{isbn}/{title}/{description}/{rating}/{pages}/{date}")
    suspend fun updateBook(
        @Path("id") bookId: Int,
        @Path("isbn") isbn: String,
        @Path("title") title: String,
        @Path("description") description: String,
        @Path("rating") rating: Double,
        @Path("pages") pages: Int,
        @Path("date") date: String,
        @Query("cover") cover: String
    ): Response<Boolean>

    @DELETE("/api/book/isbn/{isbn}/{force}")
    suspend fun deleteBook(
        @Path("isbn") isbn: String,
        @Path("force") force: Boolean
    ): Response<Boolean>

    // NEW UPDATE

    @GET("/api/bookgenre/{bookid}")
    suspend fun getBookGenres(@Path("bookid") bookId: Int): Response<List<Genre>>

    @POST("/api/bookgenre/{bookid}/{genreid}")
    suspend fun insertBookGenre(
        @Path("bookid") bookId: Int,
        @Path("genreid") genreId: Int
    ): Response<Boolean>

    @DELETE("/api/bookgenre/{bookid}/{genreid}")
    suspend fun deleteBookGenre(
        @Path("bookid") bookId: Int,
        @Path("genreid") genreId: Int
    ): Response<Boolean>

    @GET("/api/bookauthor/{bookid}")
    suspend fun getBookAuthor(@Path("bookid") bookId: Int): Response<Author>

    @POST("/api/bookauthor/{bookid}/{authorid}")
    suspend fun insertBookAuthor(
        @Path("bookid") bookId: Int,
        @Path("authorid") authorId: Int
    ): Response<Boolean>

    @DELETE("/api/bookauthor/{bookid}/{authorid}")
    suspend fun deleteBookAuthor(
        @Path("bookid") bookId: Int,
        @Path("authorid") authorId: Int
    ): Response<Boolean>

    @GET("/api/booklang/{bookid}")
    suspend fun getBookLang(@Path("bookid") bookId: Int): Response<Language>

    @POST("/api/booklang/{bookid}/{langid}")
    suspend fun insertBookLang(
        @Path("bookid") bookId: Int,
        @Path("langid") langId: Int
    ): Response<Boolean>

    @DELETE("/api/booklang/{bookid}/{langid}")
    suspend fun deleteBookLang(
        @Path("bookid") bookId: Int,
        @Path("langid") langId: Int
    ): Response<Boolean>

    @GET("/api/booklibrary/{bookid}")
    suspend fun getBookLibrary(@Path("bookid") bookId: Int): Response<List<LibraryExtended>>

    @POST("/api/booklibrary/{bookid}/{libraryid}/{copies}")
    suspend fun insertBookLibrary(
        @Path("bookid") bookId: Int,
        @Path("libraryid") libraryId: Int,
        @Path("copies") copies: Int
    ): Response<Boolean>

    @PUT("/api/booklibrary/{bookid}/{libraryid}/{copies}")
    suspend fun updateBookLibrary(
        @Path("bookid") bookId: Int,
        @Path("libraryid") libraryId: Int,
        @Path("copies") copies: Int
    ): Response<Boolean>

    @DELETE("/api/booklibrary/{bookid}/{libraryid}")
    suspend fun deleteBookLibrary(
        @Path("bookid") bookId: Int,
        @Path("libraryid") libraryId: Int
    ): Response<Boolean>
}