package com.example.bookbuddy.models

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.example.bookbuddy.R


@GlideModule
class MyAppGlideModule : AppGlideModule()

object MyGlide {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private val glide: RequestManager by lazy {
        Glide.with(context).setDefaultRequestOptions(
            RequestOptions()
                //.placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_error)
        )
    }

    fun load(url: String?, imageView: ImageView) {
        glide.load(url).into(imageView)
    }
}