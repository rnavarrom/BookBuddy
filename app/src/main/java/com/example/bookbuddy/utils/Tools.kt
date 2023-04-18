package com.example.bookbuddy.utils

import com.example.bookbuddy.api.CrudApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Tools {
    companion object {
        fun isEmailValid(email: String): Boolean {
            val regex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            return regex.matches(email)
        }

        fun isPasswordValid(password: String): Boolean {
            val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
            return regex.matches(password)
        }

        fun isValidText(text: String): Boolean{
            val regex = Regex("[a-zA-Z0-9._-]")
            return regex.matches(text)
        }

        fun isNameAviable(userName: String): Boolean {
            var response = false
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    response = crudApi.getUserExists(userName)
                }
                corrutina.join()
            }
            return response
        }

        fun isEmailAviable(email: String): Boolean{
            var response = false
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    response = crudApi.getEmailExists(email)
                }
                corrutina.join()
            }
            return response
        }
    }
}

