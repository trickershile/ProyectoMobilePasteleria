package com.example.ejemploprueba.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.DataBase.ProductoDAO
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.Model.ProductoResponseDTO

class ProductoRepository(private val dao: ProductoDAO) {
    val allProductos: LiveData<List<Producto>> = dao.getAll()

    suspend fun refreshProductos() {
        try {
            val response = RetrofitClient.instance.getProductosDisponibles()
            if (response.isSuccessful) {
                val remotos = response.body() ?: emptyList()
                val locales = remotos.map {
                    Producto(
                        id = it.id,
                        nombre = it.nombre,
                        descripcion = it.descripcion,
                        precio = it.precio,
                        stock = it.stock ?: 0,
                        categoria = it.categoria ?: "",
                        imagen = it.imagen ?: ""
                    )
                }
                dao.insertAll(locales)
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error: ${e.message}")
        }
    }

    suspend fun addProducto(producto: Producto) = dao.insert(producto)
}