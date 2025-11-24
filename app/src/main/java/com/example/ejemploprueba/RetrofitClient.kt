package com.example.ejemploprueba.API

import com.example.ejemploprueba.BuildConfig
import com.example.ejemploprueba.utils.TokenStore
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    val instance: PasteleriaApi by lazy {
        val authInterceptor = Interceptor { chain ->
            val t = TokenStore.token
            val original = chain.request()
            val hasAuth = original.header("Authorization") != null
            val builder = original.newBuilder()
            if (!hasAuth && t != null) {
                builder.addHeader("Authorization", "Bearer $t")
            }
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PasteleriaApi::class.java)
    }
}

fun parseApiError(body: okhttp3.ResponseBody?): com.example.ejemploprueba.Model.ErrorResponseDTO? {
    return try {
        val s = body?.string() ?: return null
        com.google.gson.Gson().fromJson(s, com.example.ejemploprueba.Model.ErrorResponseDTO::class.java)
    } catch (_: Exception) { null }
}
