package com.example.bookbuddy.utils

import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

interface ApiErrorListener {
    fun onApiError(connectionFailed: Boolean = false)
}

// Handle when the call is succesful or failed in some way
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>,
    errorListener: ApiErrorListener
): T? {
    try {
        val response = apiCall.invoke()
        return if (response.isSuccessful) {
            response.body()
        } else {
            errorListener.onApiError()
            null
        }
    } catch (e: SocketTimeoutException) {
        errorListener.onApiError(true)
    } catch (e: ConnectException) {
        errorListener.onApiError(true)
    } catch (e: Throwable) {
        e.printStackTrace()
        errorListener.onApiError()
    }
    return null
}