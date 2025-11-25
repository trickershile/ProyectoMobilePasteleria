package com.example.ejemploprueba.utils

import com.example.ejemploprueba.API.PasteleriaApi
import com.example.ejemploprueba.Model.ProductoResponseDTO
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductUploaderTest {
    private fun api(server: MockWebServer): PasteleriaApi {
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PasteleriaApi::class.java)
    }

    @Test
    fun crearProductoConImagen_exito() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{
                  "id": 1,
                  "nombre": "Torta Fresa",
                  "descripcion": "Bizcochuelo",
                  "precio": "25.50",
                  "urlImagen": "https://example.com/img.jpg",
                  "stock": 10,
                  "stockMinimo": 5
                }"""
            )
        )
        server.start()
        val uploader = ProductUploader(api(server))
        val res: ProductoResponseDTO = uploader.crearConBase64(
            token = "tkn",
            nombre = "Torta Fresa",
            precio = 25.50,
            cantidadInicial = 10,
            base64OrDataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAA=="
        )
        assertEquals("Torta Fresa", res.nombre)
        server.shutdown()
    }

    @Test(expected = IllegalStateException::class)
    fun crearProductoConImagen_error400() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{
                  "error": "Error al crear producto",
                  "mensaje": "cantidadInicial: obligatorio"
                }"""
            )
        )
        server.start()
        val uploader = ProductUploader(api(server))
        uploader.crearConBase64(
            token = "tkn",
            nombre = "Torta Fresa",
            precio = 25.50,
            cantidadInicial = 0,
            base64OrDataUri = "/9j/4AAQSkZJRgABAQAAAA=="
        )
        assertTrue(false)
        server.shutdown()
    }
}

