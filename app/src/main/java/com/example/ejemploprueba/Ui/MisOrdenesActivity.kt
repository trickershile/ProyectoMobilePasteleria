package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.databinding.ActivityMisOrdenesBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class MisOrdenesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMisOrdenesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OrdenesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisOrdenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        cargarOrdenes()
    }

    private fun setupRecyclerView() {
        adapter = OrdenesAdapter(
            onCancelar = { orden -> confirmarCancelar(orden) },
            onVer = { orden -> verDetalles(orden) }
        )
        binding.rvOrdenes.apply {
            adapter = this@MisOrdenesActivity.adapter
            layoutManager = LinearLayoutManager(this@MisOrdenesActivity)
        }
    }

    private fun cargarOrdenes() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getMisPedidos(
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val ordenes = response.body() ?: emptyList()

                    if (ordenes.isEmpty()) {
                        binding.tvEmpty.text = "No hay pedidos"
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvOrdenes.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvOrdenes.visibility = View.VISIBLE
                        adapter.submitList(ordenes)
                    }
                } else {
                    Toast.makeText(
                        this@MisOrdenesActivity,
                        "Error al cargar órdenes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MisOrdenesActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun confirmarCancelar(orden: com.example.ejemploprueba.Model.PedidoDTO) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancelar pedido")
            .setMessage("¿Deseas cancelar el pedido #${orden.id}?")
            .setPositiveButton("Cancelar") { _, _ -> cancelarPedido(orden.id) }
            .setNegativeButton("Mantener", null)
            .show()
    }

    private fun cancelarPedido(pedidoId: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.cancelarPedido("Bearer $token", pedidoId)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MisOrdenesActivity, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                    cargarOrdenes()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "No se pudo cancelar"
                    Toast.makeText(this@MisOrdenesActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun verDetalles(orden: com.example.ejemploprueba.Model.PedidoDTO) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val respDetalles = RetrofitClient.instance.getDetallesPorPedido("Bearer $token", orden.id)
                if (respDetalles.isSuccessful) {
                    val items = respDetalles.body() ?: emptyList()
                    val texto = if (items.isEmpty()) {
                        "Sin ítems"
                    } else {
                        items.joinToString("\n") { d ->
                            "x${d.cantidad} · Producto ${d.productoId} · ${d.precioUnitario} = ${d.total}"
                        }
                    }
                    androidx.appcompat.app.AlertDialog.Builder(this@MisOrdenesActivity)
                        .setTitle("Pedido #${orden.id}")
                        .setMessage(texto)
                        .setPositiveButton("Cerrar", null)
                        .show()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(respDetalles.errorBody())
                    val msg = parsed?.mensaje ?: "Error al cargar detalles"
                    Toast.makeText(this@MisOrdenesActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }
}
