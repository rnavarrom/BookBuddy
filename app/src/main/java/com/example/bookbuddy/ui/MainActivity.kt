package com.example.bookbuddy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookbuddy.Utils.Sha
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

        var userName = intent.getStringExtra("userName")
        binding.MAEditUser.setText(userName)

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        binding.MAButtonLogin.setOnClickListener {
            var userName = binding.MAEditUser.text.toString()
            var userPassword = binding.MAEditPassword.text.toString()

            if(!userName.isBlank() && !userPassword.isBlank()){
                val response = getUsers(userName, Sha.calculateSHA(userPassword))

                if(response){
                    Toast.makeText(this, "loging in",Toast.LENGTH_LONG).show()
                    var intent = Intent(this, CreateAccountActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Incorrect user or password",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Incorrect user or password",Toast.LENGTH_LONG).show()
            }
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
}