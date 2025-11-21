package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.PedidoDTO
import com.example.ejemploprueba.databinding.ActivityPagoBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.math.BigDecimal
import java.math.RoundingMode

class PagoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPagoBinding
    private var totalActual: String = "$0.00"
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagoBinding.inflate(layoutInflater)
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

        cargarResumen()

        binding.btnConfirmarPago.setOnClickListener {
            validarYPagar()
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 6101
        val ID_CARRITO = 6102
        val ID_CUENTA = 6103
        val ID_ADMIN = 6104
        val ID_CONTACT = 6105
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
                ID_CATALOGO -> startActivity(android.content.Intent(this, ProductListActivity::class.java))
                ID_CARRITO -> startActivity(android.content.Intent(this, CarritoActivity::class.java))
                ID_CUENTA -> startActivity(android.content.Intent(this, ProfileActivity::class.java))
                ID_ADMIN -> startActivity(android.content.Intent(this, AdminActivity::class.java))
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

    private fun cargarResumen() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getCarrito("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val carrito = response.body()!!
                    totalActual = carrito.total
                    val subtotal = parseMonto(carrito.total)
                    val iva = subtotal.multiply(BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP)
                    val totalConIva = subtotal.add(iva).setScale(2, RoundingMode.HALF_UP)
                    binding.tvCantidadProductos.text = "${carrito.items.size} productos"
                    binding.tvSubtotal.text = formatMonto(subtotal)
                    binding.tvIva.text = formatMonto(iva)
                    binding.tvTotalPago.text = formatMonto(totalConIva)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun validarYPagar() {
        val direccion = binding.etDireccion.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        if (direccion.isEmpty()) {
            binding.etDireccion.error = "Ingresa tu dirección"
            return
        }

        if (telefono.isEmpty()) {
            binding.etTelefono.error = "Ingresa tu teléfono"
            return
        }

        procesarPago(direccion, telefono)
    }

    private fun procesarPago(direccion: String, telefono: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val crearPedidoResp = RetrofitClient.instance.crearPedidoDesdeCarrito(
                    token = "Bearer $token",
                    body = mapOf("direccion" to "$direccion - Tel: $telefono")
                )

                if (crearPedidoResp.isSuccessful && crearPedidoResp.body() != null) {
                    val pedido: PedidoDTO = crearPedidoResp.body()!!
                    val montoPedido = parseMonto(pedido.total)
                    val iva = montoPedido.multiply(BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP)
                    val totalConIva = montoPedido.add(iva).setScale(2, RoundingMode.HALF_UP)
                    val pagoResp = RetrofitClient.instance.crearPago(
                        token = "Bearer $token",
                        body = mapOf(
                            "pedidoId" to pedido.id,
                            "monto" to formatMonto(totalConIva),
                            "metodo" to "EFECTIVO"
                        )
                    )

                    if (binding.chSolicitarEnvio.isChecked) {
                        try {
                            RetrofitClient.instance.crearEnvio(
                                token = "Bearer $token",
                                body = com.example.ejemploprueba.Model.CrearEnvioDTO(pedidoId = pedido.id, empresa = null)
                            )
                        } catch (_: Exception) {}
                    }

                    RetrofitClient.instance.vaciarCarrito("Bearer $token")
                    Toast.makeText(
                        this@PagoActivity,
                        "¡Pedido realizado con éxito!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@PagoActivity,
                        "Error al procesar el pedido",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PagoActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun parseMonto(monto: String): BigDecimal {
        val clean = monto.replace("$", "").replace(" ", "").replace(",", "")
        return try {
            BigDecimal(clean)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun formatMonto(valor: BigDecimal): String {
        return "$" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnConfirmarPago.isEnabled = !loading
    }
}