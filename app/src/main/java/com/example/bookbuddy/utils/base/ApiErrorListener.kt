package com.example.bookbuddy.utils.base

import org.json.JSONObject
import retrofit2.Response
import java.lang.reflect.InvocationTargetException
import java.net.ConnectException
import java.net.SocketTimeoutException

interface ApiErrorListener {
    fun onApiError() //errorMessage: String
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
            errorListener.onApiError() //errorMessage ?: errorMessage
            return null
        }
    } catch (e: SocketTimeoutException) {
        errorListener.onApiError() //"Cannot reach the server"
    } catch (e: ConnectException){
        errorListener.onApiError() //"Cannot reach the server"
    } catch (e: Throwable) {
        //errorListener.onApiError("Error fetching data: ${e.message}")
        e.printStackTrace()
        errorListener.onApiError() //errorMessage
    }
    return null
}