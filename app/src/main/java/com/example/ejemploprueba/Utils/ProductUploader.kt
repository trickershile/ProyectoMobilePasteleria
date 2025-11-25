package com.example.ejemploprueba.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.ejemploprueba.API.PasteleriaApi
import java.io.File
import java.util.Locale

class ProductUploader(private val api: PasteleriaApi) {
    private fun textPart(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun guessImageMimeType(fileName: String?): String =
        if ((fileName ?: "").lowercase(Locale.ROOT).endsWith(".png")) "image/png" else "image/jpeg"

    private fun buildParts(
        nombre: String,
        precio: Double,
        cantidadInicial: Int,
        descripcion: String? = null,
        categoriaId: Int? = null
    ): Map<String, RequestBody> {
        val parts = mutableMapOf<String, RequestBody>()
        parts["nombre"] = textPart(nombre)
        parts["precio"] = textPart(String.format(Locale.US, "%.2f", precio))
        parts["cantidadInicial"] = textPart(cantidadInicial.toString())
        if (!descripcion.isNullOrBlank()) parts["descripcion"] = textPart(descripcion)
        if (categoriaId != null) parts["categoriaId"] = textPart(categoriaId.toString())
        return parts
    }

    private fun imagePartFromFile(file: File, partName: String = "imagen"): MultipartBody.Part {
        val mime = guessImageMimeType(file.name).toMediaTypeOrNull()
        val rb = file.asRequestBody(mime)
        return MultipartBody.Part.createFormData(partName, file.name, rb)
    }

    private fun imagePartFromBytes(bytes: ByteArray, fileName: String = "imagen.jpg", partName: String = "imagen"): MultipartBody.Part {
        val mime = guessImageMimeType(fileName).toMediaTypeOrNull()
        val rb = bytes.toRequestBody(mime)
        return MultipartBody.Part.createFormData(partName, fileName, rb)
    }

    suspend fun crearConBase64(
        token: String,
        nombre: String,
        precio: Double,
        cantidadInicial: Int,
        base64OrDataUri: String,
        descripcion: String? = null,
        categoriaId: Int? = null
    ): com.example.ejemploprueba.Model.ProductoResponseDTO {
        val raw = ImageUtils.stripDataPrefix(base64OrDataUri) ?: throw IllegalArgumentException("Imagen inválida")
        val bytes = try { android.util.Base64.decode(raw, android.util.Base64.DEFAULT) } catch (e: Exception) { throw IllegalArgumentException("Base64 inválido") }
        val parts = buildParts(nombre, precio, cantidadInicial, descripcion, categoriaId)
        val imagenPart = imagePartFromBytes(bytes)
        val resp = api.crearProductoConImagen("Bearer $token", parts, imagenPart)
        if (resp.isSuccessful && resp.body() != null) return resp.body()!!
        val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
        throw IllegalStateException(parsed?.mensaje ?: "Error al crear producto")
    }

    suspend fun crearConFile(
        token: String,
        nombre: String,
        precio: Double,
        cantidadInicial: Int,
        file: File,
        descripcion: String? = null,
        categoriaId: Int? = null
    ): com.example.ejemploprueba.Model.ProductoResponseDTO {
        val parts = buildParts(nombre, precio, cantidadInicial, descripcion, categoriaId)
        val imagenPart = imagePartFromFile(file)
        val resp = api.crearProductoConImagen("Bearer $token", parts, imagenPart)
        if (resp.isSuccessful && resp.body() != null) return resp.body()!!
        val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
        throw IllegalStateException(parsed?.mensaje ?: "Error al crear producto")
    }

    suspend fun crearConUri(
        context: Context,
        token: String,
        nombre: String,
        precio: Double,
        cantidadInicial: Int,
        uri: Uri,
        descripcion: String? = null,
        categoriaId: Int? = null
    ): com.example.ejemploprueba.Model.ProductoResponseDTO {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: throw IllegalArgumentException("Imagen inválida")
        val parts = buildParts(nombre, precio, cantidadInicial, descripcion, categoriaId)
        val imagenPart = imagePartFromBytes(bytes)
        val resp = api.crearProductoConImagen("Bearer $token", parts, imagenPart)
        if (resp.isSuccessful && resp.body() != null) return resp.body()!!
        val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
        throw IllegalStateException(parsed?.mensaje ?: "Error al crear producto")
    }
}

