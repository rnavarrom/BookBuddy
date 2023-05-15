package com.example.bookbuddy.utils.base

import com.example.bookbuddy.models.Test.User
import retrofit2.Response
import java.net.SocketTimeoutException

interface ApiErrorListener {
    fun onApiError(errorMessage: String)
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>,
    errorListener: ApiErrorListener,
    errorMessage: String = "Ha ocurrido un error. Por favor, inténtalo de nuevo."
    //errorListener: ApiErrorListener
): T? {
    try {
        val response = apiCall.invoke()
        if (response.isSuccessful) {
            return response.body()
        } else {
            //val errorResponse = response.errorBody()?.string()
            //errorListener.onApiError("Error fetching data: $errorResponse")
            errorListener.onApiError(errorMessage ?: errorMessage)
        }
    } catch (e: SocketTimeoutException) {
        errorListener.onApiError("Cannot reach the server")
    } catch (e: Throwable) {
        //errorListener.onApiError("Error fetching data: ${e.message}")
        errorListener.onApiError(errorMessage)
    }
    return null
}