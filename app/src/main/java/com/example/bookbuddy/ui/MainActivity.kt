package com.example.bookbuddy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.api.logging
import com.example.bookbuddy.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor

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