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
        } else if (response.code() == 400) {
            // La respuesta es un BadRequest (código de estado 400)

            val errorBody = response.errorBody()?.string().toString()
            errorListener.onApiError() //errorBody

            // Hacer algo con el mensaje de error
            return null
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
        errorListener.onApiError() //errorMessage
    }
    return null
}