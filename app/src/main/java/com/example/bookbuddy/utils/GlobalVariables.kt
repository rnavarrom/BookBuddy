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

class UnsafeOkHttpClient {
    companion object {
        fun getUnsafeOkHttpClient(): OkHttpClient {
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {
                        }

                        override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {
                        }

                        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String?, session: SSLSession?): Boolean {
                        return true
                    }
                })

                return builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

@GlideModule
class UnsafeOkHttpGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Create an unsafe OkHttpClient that trusts any certificate
        val okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()
        // Register it as the default network loader for Glide
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
    }
}

