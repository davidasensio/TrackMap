package com.handysparksoft.trackmap.data.server

import android.os.Build
import android.util.Log
import com.google.gson.GsonBuilder
import com.handysparksoft.trackmap.BuildConfig
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Loggable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TrackMapDb {
    private val baseUrl = "https://local-grove-239221.firebaseio.com/"

    private fun getOkhttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
                okHttpClientBuilder.addInterceptor(this)
            }
            CurlInterceptor(Loggable {
                Log.d("Ok2Curl", it)
            }).apply {
                okHttpClientBuilder.addInterceptor(this)
            }
        }
        return OkHttpClient.Builder().build()
    }

    val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getOkhttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TrackMapService::class.java)
}
