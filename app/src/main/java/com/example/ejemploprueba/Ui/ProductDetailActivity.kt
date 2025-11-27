package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.CarritoAgregarRequest
import com.example.ejemploprueba.Model.CarritoItem
import com.example.ejemploprueba.databinding.ActivityProductDetailBinding
import com.example.ejemploprueba.utils.SessionManager
import com.example.ejemploprueba.utils.CarritoManager
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var carritoManager: CarritoManager
    private var productoId: Int = 0
    private var cantidad: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        carritoManager = CarritoManager(this)

        productoId = intent.getIntExtra("producto_id", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val precio = intent.getStringExtra("precio") ?: ""
        val stock = intent.getIntExtra("stock", 0)
        val categoria = intent.getStringExtra("categoria") ?: ""
        val imagen = intent.getStringExtra("imagen") ?: ""

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvName.text = nombre
        binding.tvPrice.text = "$${precio}"
        binding.tvDescription.text = descripcion
        binding.tvCategoria.text = categoria
        binding.tvStock.text = "Stock: ${stock}"
        val safe = imagen.takeIf { it.isNotBlank() }
        Glide.with(this)
            .load(safe ?: android.R.drawable.ic_menu_report_image)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.ic_menu_report_image)
            .into(binding.ivImage)

        binding.tvCantidad.text = cantidad.toString()
        binding.btnMenos.setOnClickListener {
            if (cantidad > 1) {
                cantidad -= 1
                binding.tvCantidad.text = cantidad.toString()
            }
        }
        binding.btnMas.setOnClickListener {
            cantidad += 1
            binding.tvCantidad.text = cantidad.toString()
        }

        binding.btnAgregar.setOnClickListener { agregarAlCarrito() }
    }

    private fun agregarAlCarrito() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = withRetry {
                    RetrofitClient.instance.agregarProductoCarrito(
                        token = "Bearer $token",
                        request = CarritoAgregarRequest(productoId = productoId, cantidad = cantidad)
                    )
                }
                if (response.isSuccessful) {
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "Agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .setAction("Ver") { startActivity(android.content.Intent(this@ProductDetailActivity, CarritoActivity::class.java)) }
                        .show()
                } else {
                    val nombre = binding.tvName.text.toString()
                    val precio = binding.tvPrice.text.toString().replace("$", "")
                    val imagen = (intent.getStringExtra("imagen") ?: "")
                    val item = CarritoItem(
                        detalleId = productoId,
                        productoId = productoId,
                        nombre = nombre,
                        precio = precio,
                        cantidad = cantidad,
                        imagen = imagen
                    )
                    carritoManager.agregarProducto(item)
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "Agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .setAction("Ver") { startActivity(android.content.Intent(this@ProductDetailActivity, CarritoActivity::class.java)) }
                        .show()
                }
            } catch (e: Exception) {
                val nombre = binding.tvName.text.toString()
                val precio = binding.tvPrice.text.toString().replace("$", "")
                val imagen = (intent.getStringExtra("imagen") ?: "")
                val item = CarritoItem(
                    detalleId = productoId,
                    productoId = productoId,
                    nombre = nombre,
                    precio = precio,
                    cantidad = cantidad,
                    imagen = imagen
                )
                carritoManager.agregarProducto(item)
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                    .setAction("Ver") { startActivity(android.content.Intent(this@ProductDetailActivity, CarritoActivity::class.java)) }
                    .show()
            } finally {
                showLoading(false)
            }
        }
    }

    private suspend fun <T> withRetry(block: suspend () -> retrofit2.Response<T>): retrofit2.Response<T> {
        var last: retrofit2.Response<T>? = null
        val delays = listOf(0L, 500L, 1200L)
        for (d in delays) {
            if (d > 0) kotlinx.coroutines.delay(d)
            try {
                val resp = block()
                if (resp.isSuccessful) return resp
                last = resp
            } catch (_: Exception) { }
        }
        return last ?: block()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAgregar.isEnabled = !loading
    }
}
