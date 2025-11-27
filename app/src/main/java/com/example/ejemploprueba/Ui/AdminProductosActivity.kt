package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.Model.ProductoResponseDTO
import com.example.ejemploprueba.Model.toLocal
import com.example.ejemploprueba.databinding.ActivityAdminProductosBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminProductosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminProductosBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AdminProductoAdapter
    private var productosAll: List<Producto> = emptyList()
    private var categoriasAll: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProductosBinding.inflate(layoutInflater)
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
        cargarProductos()

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminProductoAdapter(
            onEdit = { producto -> editarProducto(producto) },
            onDelete = { producto -> confirmarEliminar(producto) }
        )

        binding.rvProductos.apply {
            adapter = this@AdminProductosActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminProductosActivity)
        }

        binding.etSearchProductos.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                if (q.length >= 3) {
                    buscarProductosRemotos(q)
                } else {
                    val filtered = if (q.isEmpty()) productosAll else productosAll.filter {
                        it.nombre.lowercase().contains(q)
                    }
                    adapter.submitList(filtered)
                }
            }
        })

        cargarCategorias()
        binding.spCategoria.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val sel = if (position in categoriasAll.indices) categoriasAll[position] else "Todas"
                val filtered = if (sel == "Todas") productosAll else productosAll.filter { it.categoria.equals(sel, true) }
                adapter.submitList(filtered)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun cargarProductos() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProductos()

                if (response.isSuccessful) {
                    val productosRemotos = response.body() ?: emptyList()
                    val productos = productosRemotos.map { it.toLocal() }
                    productosAll = productos

                    if (productos.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.btnRetryProductos.visibility = View.VISIBLE
                        binding.rvProductos.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.btnRetryProductos.visibility = View.GONE
                        binding.rvProductos.visibility = View.VISIBLE
                        adapter.submitList(productos)
                    }
                } else {
                    Toast.makeText(
                        this@AdminProductosActivity,
                        "Error al cargar productos",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnRetryProductos.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminProductosActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnRetryProductos.visibility = View.VISIBLE
            } finally {
                showLoading(false)
            }
        }
    }

    private fun buscarProductosRemotos(query: String) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.buscarProductos(query, 0, 50)
                if (resp.isSuccessful && resp.body() != null) {
                    val page = resp.body()!!
                    val productos = page.content.map {
                        Producto(
                            id = it.id,
                            nombre = it.nombre,
                            descripcion = it.descripcion,
                            precio = it.precio,
                            stock = it.stock ?: 0,
                            categoria = it.categoria ?: "",
                            imagen = (it.urlImagen ?: it.imagen) ?: ""
                        )
                    }
                    adapter.submitList(productos)
                    binding.btnRetryProductos.visibility = View.GONE
                }
            } catch (_: Exception) {}
        }
    }

    private fun cargarCategorias() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getCategoriasActivas()
                if (resp.isSuccessful && resp.body() != null) {
                    categoriasAll = listOf("Todas") + resp.body()!!.map { it.nombre }
                    val spAdapter = android.widget.ArrayAdapter(this@AdminProductosActivity, android.R.layout.simple_spinner_dropdown_item, categoriasAll)
                    binding.spCategoria.adapter = spAdapter
                }
            } catch (_: Exception) {}
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 5001
        val ID_ADMIN = 5002
        val ID_USUARIOS = 5003
        val ID_PRODUCTOS = 5004
        val ID_ORDENES = 5005
        val ID_PAGOS = 5006
        val ID_INVENTARIO = 5007
        val ID_CATEGORIAS = 5008
        menu.clear()
        menu.add(android.view.Menu.NONE, ID_CATALOGO, android.view.Menu.NONE, "Catálogo").setIcon(android.R.drawable.ic_menu_view)
        menu.add(android.view.Menu.NONE, ID_ADMIN, android.view.Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        menu.add(android.view.Menu.NONE, ID_USUARIOS, android.view.Menu.NONE, "Usuarios").setIcon(android.R.drawable.ic_menu_myplaces)
        menu.add(android.view.Menu.NONE, ID_PRODUCTOS, android.view.Menu.NONE, "Productos").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(android.view.Menu.NONE, ID_ORDENES, android.view.Menu.NONE, "Órdenes").setIcon(android.R.drawable.ic_menu_agenda)
        menu.add(android.view.Menu.NONE, ID_PAGOS, android.view.Menu.NONE, "Pagos").setIcon(android.R.drawable.ic_menu_info_details)
        menu.add(android.view.Menu.NONE, ID_INVENTARIO, android.view.Menu.NONE, "Inventario").setIcon(android.R.drawable.ic_menu_day)
        menu.add(android.view.Menu.NONE, ID_CATEGORIAS, android.view.Menu.NONE, "Categorías").setIcon(android.R.drawable.ic_menu_edit)
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_CATALOGO -> startActivity(android.content.Intent(this, ProductListActivity::class.java))
                ID_ADMIN -> startActivity(android.content.Intent(this, AdminActivity::class.java))
                ID_USUARIOS -> startActivity(android.content.Intent(this, AdminUsuariosActivity::class.java))
                ID_PRODUCTOS -> {}
                ID_ORDENES -> startActivity(android.content.Intent(this, AdminOrdenesActivity::class.java))
                ID_PAGOS -> startActivity(android.content.Intent(this, AdminPagosActivity::class.java))
                ID_INVENTARIO -> startActivity(android.content.Intent(this, AdminInventarioActivity::class.java))
                ID_CATEGORIAS -> startActivity(android.content.Intent(this, AdminCategoriasActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    

    private fun editarProducto(producto: Producto) {
        val intent = Intent(this, EditProductActivity::class.java)
        intent.putExtra("producto_id", producto.id)
        startActivity(intent)
    }

    private fun confirmarEliminar(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto(id: Int) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.eliminarProducto(
                    token = "Bearer $token",
                    id = id
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminProductosActivity,
                        "Producto eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarProductos()
                } else {
                    Toast.makeText(
                        this@AdminProductosActivity,
                        "Error al eliminar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminProductosActivity,
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

    override fun onResume() {
        super.onResume()
        binding.btnRetryProductos.setOnClickListener { cargarProductos() }
        cargarProductos()
    }
}
