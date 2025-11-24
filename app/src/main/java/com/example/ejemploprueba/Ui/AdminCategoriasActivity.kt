package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.CategoriaRequestDTO
import com.example.ejemploprueba.Model.CategoriaResponseDTO
import com.example.ejemploprueba.databinding.ActivityAdminCategoriasBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminCategoriasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCategoriasBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: CategoriasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupRecyclerView()
        cargarCategorias()

        binding.fabAddCategoria.setOnClickListener { mostrarDialogoCrear() }
    }

    private fun setupRecyclerView() {
        adapter = CategoriasAdapter(
            onEdit = { categoria -> mostrarDialogoEditar(categoria) },
            onToggle = { categoria -> confirmarToggle(categoria) },
            onDelete = { categoria -> confirmarEliminar(categoria) }
        )
        binding.rvCategorias.apply {
            adapter = this@AdminCategoriasActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminCategoriasActivity)
        }
    }

    private fun cargarCategorias() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCategorias()
                if (response.isSuccessful) {
                    val categorias = response.body() ?: emptyList()
                    if (categorias.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvCategorias.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvCategorias.visibility = View.VISIBLE
                        adapter.submitList(categorias)
                    }
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al cargar categorías"
                    Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminCategoriasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun mostrarDialogoCrear() {
        val input = EditText(this)
        input.hint = "Nombre"
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(48, 16, 48, 0)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Crear Categoría")
            .setView(container)
            .setPositiveButton("Crear") { _, _ -> crearCategoria(input.text.toString().trim()) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearCategoria(nombre: String) {
        if (nombre.isEmpty()) return
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.crearCategoria("Bearer $token", CategoriaRequestDTO(nombre))
                if (response.isSuccessful) {
                    cargarCategorias()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al crear"
                    Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun mostrarDialogoEditar(categoria: CategoriaResponseDTO) {
        val input = EditText(this)
        input.setText(categoria.nombre)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(48, 16, 48, 0)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Editar Categoría")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ -> actualizarCategoria(categoria.id, input.text.toString().trim()) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarCategoria(id: Int, nombre: String) {
        if (nombre.isEmpty()) return
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.actualizarCategoria("Bearer $token", id, CategoriaRequestDTO(nombre))
                if (response.isSuccessful) {
                    cargarCategorias()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al actualizar"
                    Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun confirmarToggle(categoria: CategoriaResponseDTO) {
        val accion = if (categoria.activa) "Desactivar" else "Activar"
        AlertDialog.Builder(this)
            .setTitle("$accion Categoría")
            .setMessage("¿Deseas $accion ${categoria.nombre}?")
            .setPositiveButton(accion) { _, _ -> toggleCategoria(categoria) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleCategoria(categoria: CategoriaResponseDTO) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                if (categoria.activa) {
                    val response = RetrofitClient.instance.desactivarCategoria("Bearer $token", categoria.id)
                    if (response.isSuccessful) {
                        cargarCategorias()
                    } else {
                        val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                        val msg = parsed?.mensaje ?: "Error al desactivar"
                        Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val response = RetrofitClient.instance.actualizarCategoria("Bearer $token", categoria.id, CategoriaRequestDTO(categoria.nombre))
                    if (response.isSuccessful) {
                        cargarCategorias()
                    } else {
                        val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                        val msg = parsed?.mensaje ?: "Error al activar"
                        Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun confirmarEliminar(categoria: CategoriaResponseDTO) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Categoría")
            .setMessage("¿Deseas eliminar ${categoria.nombre}? Esta acción no se puede deshacer")
            .setPositiveButton("Eliminar") { _, _ -> eliminarCategoria(categoria.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCategoria(id: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.desactivarCategoria("Bearer $token", id)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminCategoriasActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    cargarCategorias()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al eliminar"
                    Toast.makeText(this@AdminCategoriasActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
