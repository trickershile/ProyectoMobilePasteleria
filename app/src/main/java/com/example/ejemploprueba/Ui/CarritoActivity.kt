package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.CarritoDTO
import com.example.ejemploprueba.Model.CarritoItem
import com.example.ejemploprueba.databinding.ActivityCarritoBinding
import com.example.ejemploprueba.utils.SessionManager
import com.example.ejemploprueba.utils.CarritoManager
import kotlinx.coroutines.launch

class CarritoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarritoBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: CarritoAdapter
    private var carritoActual: CarritoDTO? = null
    private lateinit var carritoManager: CarritoManager
    private var isLocalCart: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        carritoManager = CarritoManager(this)

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
        cargarCarrito()

        binding.btnPagar.setOnClickListener {
            if (isLocalCart) {
                val items = carritoManager.getItems()
                if (items.isNotEmpty()) {
                    Toast.makeText(this, "Para pagar necesitas un perfil de cliente activo.", Toast.LENGTH_LONG).show()
                }
            } else {
                if (carritoActual?.items?.isNotEmpty() == true) {
                    startActivity(Intent(this, PagoActivity::class.java))
                }
            }
        }

        binding.btnVaciar.setOnClickListener {
            vaciarCarrito()
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 4001
        val ID_CARRITO = 4002
        val ID_CUENTA = 4003
        val ID_ADMIN = 4004
        val ID_CONTACT = 4005
        menu.clear()
        menu.add(android.view.Menu.NONE, ID_CATALOGO, android.view.Menu.NONE, "Catálogo").setIcon(android.R.drawable.ic_menu_view)
        menu.add(android.view.Menu.NONE, ID_CARRITO, android.view.Menu.NONE, "Carrito").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(android.view.Menu.NONE, ID_CUENTA, android.view.Menu.NONE, "Cuenta").setIcon(android.R.drawable.ic_menu_myplaces)
        if ((sessionManager.getUserRole() ?: "Cliente") == "Administrador") {
            menu.add(android.view.Menu.NONE, ID_ADMIN, android.view.Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        } else {
            menu.add(android.view.Menu.NONE, ID_CONTACT, android.view.Menu.NONE, "Contactar").setIcon(android.R.drawable.ic_dialog_email)
        }
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_CATALOGO -> startActivity(Intent(this, ProductListActivity::class.java))
                ID_CARRITO -> {}
                ID_CUENTA -> startActivity(Intent(this, ProfileActivity::class.java))
                ID_ADMIN -> startActivity(Intent(this, AdminActivity::class.java))
                ID_CONTACT -> contactarNegocio()
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun contactarNegocio() {
        val email = getString(com.example.ejemploprueba.R.string.contact_email)
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$email")
            putExtra(android.content.Intent.EXTRA_SUBJECT, getString(com.example.ejemploprueba.R.string.app_name))
        }
        startActivity(android.content.Intent.createChooser(intent, "Contactar"))
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(
            onCantidadChanged = { detalleId, cantidad ->
                actualizarCantidad(detalleId, cantidad)
            },
            onEliminar = { detalleId ->
                eliminarDetalle(detalleId)
            }
        )

        binding.rvCarrito.apply {
            adapter = this@CarritoActivity.adapter
            layoutManager = LinearLayoutManager(this@CarritoActivity)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
        }
    }

    private fun renderCarrito(items: List<CarritoItem>, total: String) {
        if (items.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvCarrito.visibility = View.GONE
            binding.layoutTotal.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvCarrito.visibility = View.VISIBLE
            binding.layoutTotal.visibility = View.VISIBLE

            adapter.submitList(items)
            binding.tvTotal.text = total
        }
    }

    private fun cargarCarrito() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = if (token.isNotBlank()) RetrofitClient.instance.getCarrito("Bearer $token") else null
                if (response != null && response.isSuccessful && response.body() != null) {
                    isLocalCart = false
                    carritoActual = response.body()
                    val items = carritoActual?.items ?: emptyList()
                    val total = carritoActual?.total ?: "$0.00"
                    renderCarrito(items, total)
                } else {
                    isLocalCart = true
                    val items = carritoManager.getItems()
                    val total = String.format("$%.2f", carritoManager.getTotal())
                    renderCarrito(items, total)
                }
            } catch (e: Exception) {
                isLocalCart = true
                val items = carritoManager.getItems()
                val total = String.format("$%.2f", carritoManager.getTotal())
                renderCarrito(items, total)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun actualizarCantidad(detalleId: Int, cantidad: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                if (isLocalCart) {
                    carritoManager.actualizarCantidad(detalleId, cantidad)
                    val items = carritoManager.getItems()
                    val total = String.format("$%.2f", carritoManager.getTotal())
                    renderCarrito(items, total)
                } else {
                    val token = sessionManager.getToken() ?: ""
                    val response = RetrofitClient.instance.actualizarCantidadCarrito(
                        token = "Bearer $token",
                        detalleId = detalleId,
                        body = mapOf("cantidad" to cantidad)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        carritoActual = response.body()
                        renderCarrito(carritoActual?.items ?: emptyList(), carritoActual?.total ?: "$0.00")
                    }
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun eliminarDetalle(detalleId: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                if (isLocalCart) {
                    carritoManager.eliminarProducto(detalleId)
                    val items = carritoManager.getItems()
                    val total = String.format("$%.2f", carritoManager.getTotal())
                    renderCarrito(items, total)
                    Toast.makeText(this@CarritoActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    val token = sessionManager.getToken() ?: ""
                    val response = RetrofitClient.instance.eliminarDetalleCarrito(
                        token = "Bearer $token",
                        detalleId = detalleId
                    )
                    if (response.isSuccessful && response.body() != null) {
                        carritoActual = response.body()
                        renderCarrito(carritoActual?.items ?: emptyList(), carritoActual?.total ?: "$0.00")
                        Toast.makeText(this@CarritoActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun vaciarCarrito() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                if (isLocalCart) {
                    carritoManager.limpiar()
                    renderCarrito(emptyList(), "$0.00")
                    Toast.makeText(this@CarritoActivity, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                } else {
                    val token = sessionManager.getToken() ?: ""
                    val response = RetrofitClient.instance.vaciarCarrito("Bearer $token")
                    if (response.isSuccessful) {
                        carritoActual = CarritoDTO(0, emptyList(), "$0.00")
                        renderCarrito(emptyList(), "$0.00")
                        Toast.makeText(this@CarritoActivity, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
    }
}