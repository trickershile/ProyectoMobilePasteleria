package com.example.ejemploprueba.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ejemploprueba.DataBase.AppDataBase
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.Repository.ProductoRepository
import kotlinx.coroutines.launch

class AddProductViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ProductoRepository(AppDataBase.getDatabase(app).productoDao())

    val isSaving = MutableLiveData<Boolean>()
    val saveSuccess = MutableLiveData<Boolean>()

    fun agregarNuevoProducto(nombre: String, precio: String, categoria: String, descripcion: String, urlImagen: String) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                val producto = Producto(0, nombre, descripcion, precio, 1, categoria, urlImagen)
                repository.addProducto(producto)
                saveSuccess.value = true
            } catch (e: Exception) {
                saveSuccess.value = false
            } finally {
                isSaving.value = false
            }
        }
    }
}