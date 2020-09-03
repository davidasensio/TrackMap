package com.handysparksoft.trackmap.core.data.server

import android.util.Log
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
        return okHttpClientBuilder.build()
    }

    val service: TrackMapService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getOkhttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TrackMapService::class.java)
}
