package com.example.bookbuddy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookbuddy.api.BookAPI
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityMainBinding
import com.example.bookbuddy.models.UserItem
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        binding.MAButtonLogin.setOnClickListener {
            var userName = binding.MAEditUser.text.toString()
            var userPassword = binding.MAEditPassword.text.toString()

            val response = getUsers(userName, userPassword)

            Toast.makeText(this, response.toString(),Toast.LENGTH_LONG).show()
        }

        binding.MAButtonSignIn.setOnClickListener {
            var intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    fun getUsers(userName: String, password: String) :Boolean{
        var response = false
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                response = crudApi.getUserLogin(userName, password)
            }
            corrutina.join()
        }
        return response
    }


/*
    fun postUser(user : UserItem){

        // Create Retrofit
        val retrofit = getRetrofit()

        // Create Service
        val service = retrofit.create(BookAPI::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("name", user.name)
        jsonObject.put("email", user.email)
        jsonObject.put("password", user.password)

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            val response = service.insertProducte(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                        )
                    )

                    Log.d("Pretty Printed JSON :", prettyJson)

                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }


        /*
            CoroutineScope(Dispatchers.IO).launch {
                val response = getRetrofit().create(APIService::class.java).insertProducte(prod).body()

            }

             */
    }

 */
}