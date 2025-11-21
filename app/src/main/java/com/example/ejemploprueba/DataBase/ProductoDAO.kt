package com.example.ejemploprueba.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.ejemploprueba.Model.Producto

@Dao
interface ProductoDAO {
    @Query("SELECT * FROM productos")
    fun getAll(): LiveData<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: Producto)
}