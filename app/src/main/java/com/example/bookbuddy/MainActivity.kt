package com.example.bookbuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("Facendo pruebas")
        println("Segundo commit pruebas")
        println("Tercer commit pruebas")
        println("Estamos en dev")
    }
}