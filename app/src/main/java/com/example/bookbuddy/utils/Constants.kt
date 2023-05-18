package com.example.bookbuddy.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

class Constants {
    companion object{
        const val BASE_URL = "https://172.16.24.120:7137/"
        //const val BASE_URL = "https://192.168.1.55:7137/"
        //const val BASE_URL = "https://192.168.1.53:7137/"
        //const val BASE_URL = "https://172.16.24.198:7137/"
        //const val BASE_URL = "https://172.16.24.136:7137/"
    }
}