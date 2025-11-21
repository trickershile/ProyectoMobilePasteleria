package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.PedidoDTO
import com.example.ejemploprueba.databinding.ActivityAdminOrdenesBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminOrdenesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminOrdenesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AdminOrdenesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminOrdenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            com.example.ejemploprueba.R.string.app_name,
            com.example.ejemploprueba.R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupDrawerMenu()

        setupRecyclerView()
        cargarOrdenes()
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 8001
        val ID_ADMIN = 8002
        val ID_USUARIOS = 8003
        val ID_PRODUCTOS = 8004
        val ID_ORDENES = 8005
        val ID_PAGOS = 8006
        val ID_CUENTA = 8007
        menu.clear()
        menu.add(android.view.Menu.NONE, ID_CATALOGO, android.view.Menu.NONE, "Catálogo").setIcon(android.R.drawable.ic_menu_view)
        menu.add(android.view.Menu.NONE, ID_ADMIN, android.view.Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        menu.add(android.view.Menu.NONE, ID_USUARIOS, android.view.Menu.NONE, "Usuarios").setIcon(android.R.drawable.ic_menu_myplaces)
        menu.add(android.view.Menu.NONE, ID_PRODUCTOS, android.view.Menu.NONE, "Productos").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(android.view.Menu.NONE, ID_ORDENES, android.view.Menu.NONE, "Órdenes").setIcon(android.R.drawable.ic_menu_agenda)
        menu.add(android.view.Menu.NONE, ID_PAGOS, android.view.Menu.NONE, "Pagos").setIcon(android.R.drawable.ic_menu_info_details)
        menu.add(android.view.Menu.NONE, ID_CUENTA, android.view.Menu.NONE, "Cuenta").setIcon(android.R.drawable.ic_menu_myplaces)
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_CATALOGO -> startActivity(android.content.Intent(this, ProductListActivity::class.java))
                ID_ADMIN -> startActivity(android.content.Intent(this, AdminActivity::class.java))
                ID_USUARIOS -> startActivity(android.content.Intent(this, AdminUsuariosActivity::class.java))
                ID_PRODUCTOS -> startActivity(android.content.Intent(this, AdminProductosActivity::class.java))
                ID_ORDENES -> {}
                ID_PAGOS -> startActivity(android.content.Intent(this, AdminPagosActivity::class.java))
                ID_CUENTA -> startActivity(android.content.Intent(this, ProfileActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdenesAdapter(
            onAccept = { orden -> cambiarEstado(orden, "Aceptado") },
            onReject = { orden -> cambiarEstado(orden, "Rechazado") },
            onShip = { orden -> cambiarEstado(orden, "Enviado") }
        )

        binding.rvOrdenes.apply {
            adapter = this@AdminOrdenesActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminOrdenesActivity)
        }
    }

    private fun cargarOrdenes() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getPedidosAdminTodos("Bearer $token")

                if (response.isSuccessful) {
                    val ordenes = response.body() ?: emptyList()

                    if (ordenes.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvOrdenes.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvOrdenes.visibility = View.VISIBLE
                        adapter.submitList(ordenes)
                    }
                } else {
                    Toast.makeText(
                        this@AdminOrdenesActivity,
                        "Error al cargar órdenes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminOrdenesActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun cambiarEstado(orden: PedidoDTO, nuevoEstado: String) {
        AlertDialog.Builder(this)
            .setTitle("Cambiar Estado")
            .setMessage("¿Cambiar pedido #${orden.id} a $nuevoEstado?")
            .setPositiveButton("Confirmar") { _, _ ->
                actualizarEstado(orden.id, nuevoEstado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarEstado(ordenId: Int, estado: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.actualizarEstadoPedidoAdmin(
                    token = "Bearer $token",
                    pedidoId = ordenId,
                    body = mapOf("estado" to estado)
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminOrdenesActivity,
                        "Estado actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarOrdenes()
                } else {
                    Toast.makeText(
                        this@AdminOrdenesActivity,
                        "Error al actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminOrdenesActivity,
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
}