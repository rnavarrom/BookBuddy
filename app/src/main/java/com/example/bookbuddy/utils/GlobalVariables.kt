package com.example.bookbuddy.utils

import android.content.Context
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.example.bookbuddy.models.Profile
import com.example.bookbuddy.models.User
import com.example.bookbuddy.models.UserItem
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.InputStream
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*


lateinit var navView: NavigationView
lateinit var navController : NavController
var currentUser : User? = null
lateinit var currentProfile: Profile
var currentPicture: File? = null
lateinit var currentUserCreate : UserItem
val keyboardValue : Int = 250
var currentLanguageChanged: Boolean = false

fun isProfileInitialized(): Boolean {
    return ::currentProfile.isInitialized
}
