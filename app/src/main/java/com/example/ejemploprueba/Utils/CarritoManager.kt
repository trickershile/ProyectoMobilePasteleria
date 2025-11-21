package com.example.ejemploprueba.utils

import android.content.Context
import com.example.ejemploprueba.Model.CarritoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CarritoManager(context: Context) {
    private val prefs = context.getSharedPreferences("Carrito", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun agregarProducto(item: CarritoItem) {
        val items = getItems().toMutableList()
        val existente = items.find { it.productoId == item.productoId }

        if (existente != null) {
            items[items.indexOf(existente)] = existente.copy(cantidad = existente.cantidad + item.cantidad)
        } else {
            items.add(item)
        }

        saveItems(items)
    }

    fun actualizarCantidad(productoId: Int, cantidad: Int) {
        val items = getItems().toMutableList()
        val index = items.indexOfFirst { it.productoId == productoId }

        if (index != -1) {
            if (cantidad <= 0) {
                items.removeAt(index)
            } else {
                items[index] = items[index].copy(cantidad = cantidad)
            }
            saveItems(items)
        }
    }

    fun eliminarProducto(productoId: Int) {
        val items = getItems().filter { it.productoId != productoId }
        saveItems(items)
    }

    fun getItems(): List<CarritoItem> {
        val json = prefs.getString("items", "[]")
        val type = object : TypeToken<List<CarritoItem>>() {}.type
        return try {
            gson.fromJson<List<CarritoItem>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getTotal(): Double {
        return getItems().sumOf { it.precio.toDouble() * it.cantidad }
    }

    fun getCantidadTotal(): Int {
        return getItems().sumOf { it.cantidad }
    }

    fun limpiar() {
        prefs.edit().clear().apply()
    }

    private fun saveItems(items: List<CarritoItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString("items", json).apply()
    }
}