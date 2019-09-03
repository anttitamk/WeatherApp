package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

object Function {

    fun isNetworkAvailable(context: Context): Boolean {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null
    }


    fun executeGet(targetURL: String): String? {
        val url: URL
        var connection: HttpURLConnection? = null
        return try {
            //Create connection
            url = URL(targetURL)
            connection = url.openConnection() as HttpURLConnection

            connection.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }


    fun setWeatherIcon(actualId: Int, sunrise: Long, sunset: Long): String {
        val id = actualId / 100
        var icon = ""
        if (actualId == 800) {
            icon = when (Date().time) {
                in sunrise until sunset -> "&#xf00d;"
                else -> "&#xf02e;"
            }
        } else {
            when (id) {
                2 -> icon = "&#xf01e;"
                3 -> icon = "&#xf01c;"
                7 -> icon = "&#xf014;"
                8 -> icon = "&#xf013;"
                6 -> icon = "&#xf01b;"
                5 -> icon = "&#xf019;"
            }
        }
        return icon
    }
}