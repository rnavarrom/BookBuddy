package com.example.bookbuddy.utils

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.example.bookbuddy.api.CrudApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.Normalizer

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

        fun tooglePasswordVisible(editText: EditText){
            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                //eyeToggle.setImageResource(R.drawable.ic_eye_visible)
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                //eyeToggle.setImageResource(R.drawable.ic_eye_hidden)
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        fun CharSequence.unaccent(): String {
            val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
            return REGEX_UNACCENT.replace(temp, "")
        }
    }
}

