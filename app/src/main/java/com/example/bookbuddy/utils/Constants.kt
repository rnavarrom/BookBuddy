package com.example.bookbuddy.utils

import com.bumptech.glide.request.RequestOptions
import com.example.bookbuddy.R

class Constants {
    companion object {
        const val BASE_URL = "https://172.16.24.120:7137/"
        //const val BASE_URL = "https://192.168.1.55:7137/"

        const val ErrrorMessage = "Can't reach the server. Try again!"

        // Default options for using glide, if image can't load set a default one
        val bookRequestOptions = RequestOptions()
            .placeholder(R.drawable.book_placeholder)
            .error(R.drawable.book_placeholder)

        val profileRequestOptions = RequestOptions()
            .placeholder(R.drawable.default_picture)
            .error(R.drawable.default_picture)
    }
}