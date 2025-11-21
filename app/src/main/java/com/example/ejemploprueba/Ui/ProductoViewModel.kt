package com.example.ejemploprueba.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ejemploprueba.DataBase.AppDataBase
import com.example.ejemploprueba.Repository.ProductoRepository
import kotlinx.coroutines.launch

class ProductViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ProductoRepository(AppDataBase.getDatabase(app).productoDao())
    val allProductos = repository.allProductos

    init {
        refreshData()
    }

    fun refreshData() = viewModelScope.launch {
        repository.refreshProductos()
    }
}