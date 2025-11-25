package com.example.ejemploprueba.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Base64
import android.widget.ImageView

object ImageUtils {
    fun stripDataPrefix(input: String?): String? {
        val s = input?.trim() ?: return null
        return if (s.startsWith("data:") && s.contains("base64,")) s.substringAfter("base64,").trim() else s
    }

    fun decodeBase64ToBitmap(base64OrDataUri: String?): Bitmap? {
        val raw = stripDataPrefix(base64OrDataUri) ?: return null
        return try {
            val bytes = Base64.decode(raw, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }
}

fun ImageView.loadBase64Image(base64OrDataUri: String?): Boolean {
    val bmp = try { ImageUtils.decodeBase64ToBitmap(base64OrDataUri) } catch (_: Exception) { null }
    if (bmp == null) return false
    return try {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setImageBitmap(bmp)
        } else {
            post { setImageBitmap(bmp) }
        }
        true
    } catch (_: Exception) {
        false
    }
}

