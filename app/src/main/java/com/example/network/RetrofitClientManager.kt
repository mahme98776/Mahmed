package com.example.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manages the dynamic creation of Retrofit instances pointing to the
 * user-specified server IP and Port (defaulting to 192.168.1.3:8000).
 */
class RetrofitClientManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var currentBaseUrl: String = ""
    private var currentRetrofit: Retrofit? = null
    private var currentApiService: LingoApiService? = null

    val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("LingoHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Formats IP address and Port into a clean HTTP Base URL.
     */
    fun formatBaseUrl(ip: String, port: Int): String {
        val cleanIp = ip.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")

        val protocol = if (cleanIp.contains("trycloudflare.com") || cleanIp.contains("ngrok.io") || cleanIp.contains("loca.lt")) {
            "https"
        } else {
            "http"
        }

        return if (cleanIp.contains(":") || protocol == "https") {
            // Already includes port or is HTTPS tunnel
            "$protocol://$cleanIp/"
        } else {
            "$protocol://$cleanIp:$port/"
        }
    }

    /**
     * Returns an instance of [LingoApiService] pointing to the given IP and Port.
     * Reuses the existing client instance if the URL has not changed.
     */
    @Synchronized
    fun getApiService(ip: String, port: Int): LingoApiService {
        val baseUrl = formatBaseUrl(ip, port)
        if (currentApiService != null && currentBaseUrl == baseUrl) {
            return currentApiService!!
        }

        currentBaseUrl = baseUrl
        currentRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = currentRetrofit!!.create(LingoApiService::class.java)
        currentApiService = service
        return service
    }
}
