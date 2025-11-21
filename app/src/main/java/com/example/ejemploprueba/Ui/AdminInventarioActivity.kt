package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.AgregarStockDTO
import com.example.ejemploprueba.Model.InventarioItemDTO
import com.example.ejemploprueba.Model.ReducirStockDTO
import com.example.ejemploprueba.databinding.ActivityAdminInventarioBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminInventarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInventarioBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: InventarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupRecyclerView()
        cargarInventario()

        binding.btnVerTodo.setOnClickListener { cargarInventario() }
        binding.btnBajoStock.setOnClickListener { cargarBajoStock() }
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(
            onAgregar = { item -> ajustarStock(item, 1) },
            onReducir = { item -> ajustarStock(item, -1) },
            onDisponibilidad = { item, cantidad -> verificarDisponibilidad(item.productoId, cantidad) }
        )
        binding.rvInventario.apply {
            adapter = this@AdminInventarioActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminInventarioActivity)
        }
    }

    private fun cargarInventario() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getInventarioAdmin("Bearer $token")
                if (resp.isSuccessful) {
                    val items = resp.body() ?: emptyList()
                    if (items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvInventario.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvInventario.visibility = View.VISIBLE
                        adapter.submitList(items)
                    }
                } else {
                    Toast.makeText(this@AdminInventarioActivity, "Error al cargar inventario", Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun cargarBajoStock() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getBajoStock("Bearer $token")
                if (resp.isSuccessful) {
                    val items = resp.body() ?: emptyList()
                    if (items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvInventario.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvInventario.visibility = View.VISIBLE
                        adapter.submitList(items)
                    }
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun verificarDisponibilidad(productoId: Int, cantidad: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.verificarDisponibilidad("Bearer $token", productoId, cantidad)
                if (resp.isSuccessful && resp.body() != null) {
                    val d = resp.body()!!
                    Toast.makeText(this@AdminInventarioActivity, if (d.disponible) "Disponible (${d.disponibleCantidad})" else "No disponible", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdminInventarioActivity, "Error al verificar", Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun ajustarStock(item: InventarioItemDTO, delta: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = if (delta > 0) {
                    RetrofitClient.instance.agregarStock("Bearer $token", item.productoId, AgregarStockDTO(delta))
                } else {
                    RetrofitClient.instance.reducirStock("Bearer $token", item.productoId, ReducirStockDTO(-delta))
                }
                if (resp.isSuccessful) {
                    cargarInventario()
                } else {
                    Toast.makeText(this@AdminInventarioActivity, "Error al actualizar stock", Toast.LENGTH_SHORT).show()
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