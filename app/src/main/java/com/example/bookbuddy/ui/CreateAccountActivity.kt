package com.example.bookbuddy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookbuddy.databinding.ActivityCreateAccountBinding

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}