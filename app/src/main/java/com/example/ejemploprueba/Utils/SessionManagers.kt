package com.example.ejemploprueba.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun saveAuth(token: String, userId: Int, userName: String, userEmail: String, userRole: String = "Cliente") {
        prefs.edit().apply {
            putString("token", token)
            putInt("userId", userId)
            putString("userName", userName)
            putString("userEmail", userEmail)
            putString("userRole", userRole)
            putBoolean("isLoggedIn", true)
            apply()
        }
        TokenStore.token = token
    }

    fun isLoggedIn() = try { prefs.getBoolean("isLoggedIn", false) } catch (_: ClassCastException) { false }

    fun getToken() = prefs.getString("token", null)

    fun getUserName() = prefs.getString("userName", null)

    fun getUserEmail() = prefs.getString("userEmail", null)

    fun getUserId(): Int {
        return try {
            prefs.getInt("userId", 0)
        } catch (_: ClassCastException) {
            try {
                prefs.getLong("userId", 0L).toInt()
            } catch (_: ClassCastException) {
                0
            }
        }
    }

    fun getUserRole() = prefs.getString("userRole", "Cliente")

    fun clearSession() {
        prefs.edit().clear().apply()
        TokenStore.token = null
    }
}

object TokenStore {
    @Volatile
    var token: String? = null
}