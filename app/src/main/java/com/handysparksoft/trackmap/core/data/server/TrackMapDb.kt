package com.handysparksoft.trackmap.core.data.server

import android.util.Log
import com.handysparksoft.trackmap.BuildConfig
import com.handysparksoft.trackmap.core.platform.network.ConnectionHandler
import com.handysparksoft.trackmap.core.platform.network.NetworkConnectionNotAvailableException
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Loggable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

object TrackMapDb {
    private val baseUrl = "https://local-grove-239221.firebaseio.com/"

    private fun getNetworkConnectionInterceptor(): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                if (!ConnectionHandler.isNetworkAvailable) {
                    throw NetworkConnectionNotAvailableException()
                }
                return chain.proceed(chain.request())
            }
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
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

        okHttpClientBuilder.addInterceptor(getNetworkConnectionInterceptor())

        return okHttpClientBuilder.build()
    }

    val service: TrackMapService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TrackMapService::class.java)
}
