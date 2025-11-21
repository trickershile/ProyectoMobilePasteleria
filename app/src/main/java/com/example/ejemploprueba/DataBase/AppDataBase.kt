package com.example.ejemploprueba.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ejemploprueba.Model.Producto

@Database(entities = [Producto::class], version = 2, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun productoDao(): ProductoDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "pasteleria_database"
                )
                    .fallbackToDestructiveMigration() // Esto borrará y recreará la BD
                    .build().also { INSTANCE = it }
            }
        }
    }
}