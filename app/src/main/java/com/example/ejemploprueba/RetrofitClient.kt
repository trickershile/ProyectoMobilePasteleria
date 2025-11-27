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
    @Volatile private var api: PasteleriaApi? = null
    @Volatile private var usedBaseUrl: String = BuildConfig.BASE_URL

    val instance: PasteleriaApi
        get() = get()

    fun setBaseUrlOverride(url: String?) {
        TokenStore.baseUrlOverride = url
    }

    @Synchronized
    fun get(): PasteleriaApi {
        var desiredBase = TokenStore.baseUrlOverride ?: BuildConfig.BASE_URL
        if (TokenStore.baseUrlOverride == null && isEmulator() && desiredBase.startsWith("http://192.168.")) {
            desiredBase = desiredBase.replace(Regex("http://192\\.168\\.[0-9]+\\.[0-9]+"), "http://10.0.2.2")
        }
        if (api == null || usedBaseUrl != desiredBase) {
            usedBaseUrl = desiredBase

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

            val statusInterceptor = Interceptor { chain ->
                val resp = chain.proceed(chain.request())
                if (resp.code == 401) {
                    TokenStore.token = null
                }
                resp
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(statusInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build()

            api = Retrofit.Builder()
                .baseUrl(desiredBase)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(PasteleriaApi::class.java)
        }
        return api!!
    }

    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.lowercase().contains("emulator") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.MANUFACTURER.contains("Genymotion")
    }
}

fun parseApiError(body: okhttp3.ResponseBody?): com.example.ejemploprueba.Model.ErrorResponseDTO? {
    return try {
        val s = body?.string() ?: return null
        try {
            com.google.gson.Gson().fromJson(s, com.example.ejemploprueba.Model.ErrorResponseDTO::class.java)
        } catch (_: Exception) {
            com.example.ejemploprueba.Model.ErrorResponseDTO(error = "unknown", mensaje = s)
        }
    } catch (_: Exception) { null }
}
