package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.PagoDTO
import com.example.ejemploprueba.databinding.ActivityAdminPagosBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminPagosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPagosBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AdminPagosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPagosBinding.inflate(layoutInflater)
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
        cargarPagosPendientes()

        binding.btnVerPendientes.setOnClickListener { cargarPagosPendientes() }
        binding.btnVerAprobados.setOnClickListener { cargarPorEstado("Aprobado") }
        binding.btnVerRechazados.setOnClickListener { cargarPorEstado("Rechazado") }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 9001
        val ID_ADMIN = 9002
        val ID_USUARIOS = 9003
        val ID_PRODUCTOS = 9004
        val ID_ORDENES = 9005
        val ID_PAGOS = 9006
        val ID_CUENTA = 9007
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
                ID_ORDENES -> startActivity(android.content.Intent(this, AdminOrdenesActivity::class.java))
                ID_PAGOS -> {}
                ID_CUENTA -> startActivity(android.content.Intent(this, ProfileActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminPagosAdapter(
            onAprobar = { pago -> procesarPago(pago, aprobar = true) },
            onRechazar = { pago -> procesarPago(pago, aprobar = false) }
        )
        binding.rvPagos.apply {
            adapter = this@AdminPagosActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminPagosActivity)
        }
    }

    private fun cargarPagosPendientes() = cargarPorEstado("Pendiente")

    private fun cargarPorEstado(estado: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getPagosPorEstadoAdmin("Bearer $token", estado)
                if (resp.isSuccessful) {
                    val pagos = resp.body() ?: emptyList()
                    if (pagos.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvPagos.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvPagos.visibility = View.VISIBLE
                        adapter.submitList(pagos)
                    }
                } else {
                    Toast.makeText(this@AdminPagosActivity, "Error al cargar pagos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPagosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun procesarPago(pago: PagoDTO, aprobar: Boolean) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = if (aprobar) {
                    RetrofitClient.instance.aprobarPagoAdmin("Bearer $token", pago.id)
                } else {
                    RetrofitClient.instance.rechazarPagoAdmin("Bearer $token", pago.id)
                }
                if (resp.isSuccessful) {
                    if (aprobar) {
                        RetrofitClient.instance.actualizarEstadoPedidoAdmin("Bearer $token", pago.pedidoId, mapOf("estado" to "Enviado"))
                    } else {
                        RetrofitClient.instance.actualizarEstadoPedidoAdmin("Bearer $token", pago.pedidoId, mapOf("estado" to "Rechazado"))
                    }
                    Toast.makeText(this@AdminPagosActivity, if (aprobar) "Pago aprobado y pedido enviado" else "Pago rechazado", Toast.LENGTH_SHORT).show()
                    cargarPagosPendientes()
                } else {
                    Toast.makeText(this@AdminPagosActivity, "No se pudo procesar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPagosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}