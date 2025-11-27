package com.example.ejemploprueba.Ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.BuildConfig
import com.example.ejemploprueba.Model.Producto
import com.example.ejemploprueba.Model.ProductoResponseDTO
import com.example.ejemploprueba.Model.toLocal
import com.example.ejemploprueba.Model.CarritoItem
import com.example.ejemploprueba.databinding.ActivityProductListBinding
import com.example.ejemploprueba.databinding.DialogProductDetailBinding
import com.example.ejemploprueba.utils.SessionManager
import com.example.ejemploprueba.utils.CarritoManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle

class ProductListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductListBinding
    private lateinit var adapter: ProductoAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var carritoManager: CarritoManager
    private var cartBadge: BadgeDrawable? = null
    private var allProducts: List<Producto> = emptyList()
    private var searchJob: Job? = null
    private var page = 0
    private val pageSize = 12
    private var hasMore = true
    private var isLoading = false
    private var isAddToCartInProgress = false

    companion object {
        private const val ACTION_CART_ID = 1001
        private const val ACTION_CONTACT_ID = 1002
        private const val ACTION_ENV_ID = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        RetrofitClient.setBaseUrlOverride(sessionManager.getBaseUrlOverride())
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
        setupToolbarMenu()

        setupRecyclerView()
        setupTopBadges()
        // removed observers; using direct paging fetch
        setupButtons()
        updateCarritoBadge()
        setupSearch()
        setupCategoriaFilter()
        setupInfiniteScroll()
        setupSwipeRefresh()
        setupBottomNav()
        if (allProducts.isEmpty()) {
            cargarPagina(reset = true)
        }
    }
    private fun setupTopBadges() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getTopProductos("Bearer $token", 5)
                if (resp.isSuccessful && resp.body() != null) {
                    val ids = resp.body()!!.map { it.productoId }.toSet()
                    adapter.topIds = ids
                    adapter.notifyDataSetChanged()
                }
            } catch (_: Exception) {}
        }
    }
    private fun setupRecyclerView() {
        adapter = ProductoAdapter(
            onProductClick = { producto -> showProductDetail(producto) },
            onAddToCart = { producto, view ->
                animateAddToCart(view, producto.imagen)
                agregarAlCarrito(producto)
            }
        )

        binding.rvProducts.apply {
            adapter = this@ProductListActivity.adapter
            layoutManager = LinearLayoutManager(this@ProductListActivity)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
        }
        val promoAdapter = BannerAdapter()
        binding.rvPromos.apply {
            adapter = promoAdapter
            layoutManager = LinearLayoutManager(this@ProductListActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        androidx.recyclerview.widget.PagerSnapHelper().attachToRecyclerView(binding.rvPromos)
        val promos = listOf(
            PromoBanner(null, "2x1 en Cupcakes"),
            PromoBanner(null, "Tortas al 20% OFF"),
            PromoBanner(null, "Macarons edición limitada")
        )
        promoAdapter.submit(promos)
    }

    private fun animateAddToCart(source: View, imageUrl: String) {
        val root = binding.root as android.view.ViewGroup
        val temp = android.widget.ImageView(this)
        temp.layoutParams = android.view.ViewGroup.LayoutParams(source.width, source.height)
        val src = imageUrl.takeIf { it.isNotBlank() && isLoadableImage(it) } ?: android.R.drawable.ic_menu_report_image
        try { com.bumptech.glide.Glide.with(this).load(src).into(temp) } catch (_: Exception) {}
        val srcLoc = IntArray(2)
        val dstLoc = IntArray(2)
        source.getLocationOnScreen(srcLoc)
        binding.fabCarrito.getLocationOnScreen(dstLoc)
        temp.translationX = srcLoc[0].toFloat()
        temp.translationY = srcLoc[1].toFloat()
        root.addView(temp)
        val animX = android.animation.ObjectAnimator.ofFloat(temp, android.view.View.TRANSLATION_X, dstLoc[0].toFloat())
        val animY = android.animation.ObjectAnimator.ofFloat(temp, android.view.View.TRANSLATION_Y, dstLoc[1].toFloat())
        val scaleX = android.animation.ObjectAnimator.ofFloat(temp, android.view.View.SCALE_X, 0.3f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(temp, android.view.View.SCALE_Y, 0.3f)
        val set = android.animation.AnimatorSet()
        set.playTogether(animX, animY, scaleX, scaleY)
        set.duration = 500
        set.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                root.removeView(temp)
            }
        })
        set.start()
    }

    private fun isLoadableImage(s: String): Boolean {
        val lower = s.lowercase()
        if (lower.startsWith("data:")) return false
        if (lower.startsWith("http://") || lower.startsWith("https://")) return true
        if (lower.startsWith("file://") || lower.startsWith("content://")) return true
        return false
    }

    private fun mostrarMenu() {
        val items = arrayOf("Ir al carrito", "Ajustes de la cuenta", "Contactar al negocio")
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(com.example.ejemploprueba.R.string.app_name))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, CarritoActivity::class.java))
                    1 -> startActivity(Intent(this, ProfileActivity::class.java))
                    2 -> contactarNegocio()
                }
            }
            .create()
        dialog.show()
    }


    private fun setupToolbarMenu() {
        val menu: Menu = binding.toolbar.menu
        val cartItem = menu.add(Menu.NONE, ACTION_CART_ID, Menu.NONE, "Carrito")
        cartItem.setIcon(android.R.drawable.ic_menu_sort_by_size)
        cartItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val role = sessionManager.getUserRole() ?: "Cliente"
        if (!role.equals("Administrador", true)) {
            val contactItem = menu.add(Menu.NONE, ACTION_CONTACT_ID, Menu.NONE, "Contactar")
            contactItem.setIcon(android.R.drawable.ic_dialog_email)
            contactItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        if (BuildConfig.DEBUG) {
            val envItem = menu.add(Menu.NONE, ACTION_ENV_ID, Menu.NONE, "Cambiar entorno")
            envItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                ACTION_CART_ID -> {
                    startActivity(Intent(this, CarritoActivity::class.java)); true
                }
                ACTION_CONTACT_ID -> { contactarNegocio(); true }
                ACTION_ENV_ID -> { mostrarSelectorEntorno(); true }
                else -> false
            }
        }
        // Badge handled by tvCarritoBadge overlay
    }

    private fun contactarNegocio() {
        val email = getString(com.example.ejemploprueba.R.string.contact_email)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, getString(com.example.ejemploprueba.R.string.app_name))
        }
        startActivity(Intent.createChooser(intent, "Contactar"))
    }

    // removed observers

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(250)
                    val filtered = if (q.isEmpty()) allProducts else allProducts.filter {
                        it.nombre.lowercase().contains(q)
                    }
                    adapter.submitList(filtered)
                }
            }
        })
    }

    private fun setupCategoriaFilter() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getCategoriasActivas()
                if (resp.isSuccessful && resp.body() != null) {
                    val categorias = listOf("Todas") + resp.body()!!.map { it.nombre }
                    val adapterSp = android.widget.ArrayAdapter(this@ProductListActivity, android.R.layout.simple_spinner_dropdown_item, categorias)
                    binding.spCategoria.adapter = adapterSp
                    binding.spCategoria.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            val sel = categorias[position]
                            val filtered = if (sel == "Todas") allProducts else allProducts.filter { it.categoria.equals(sel, true) }
                            adapter.submitList(filtered)
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun setupButtons() {
        // Botón de carrito
        binding.fabCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Botón de perfil
        binding.fabProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Botón de Admin (solo si es admin)
        if (sessionManager.getUserRole() == "Administrador") {
            binding.fabAdmin.visibility = View.VISIBLE
            binding.fabAdmin.setOnClickListener {
                startActivity(Intent(this, AdminActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        } else {
            binding.fabAdmin.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateCarritoBadge()
    }

    private fun updateCarritoBadge() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = if (token.isNotBlank()) RetrofitClient.instance.getCarrito("Bearer $token") else null
                if (response != null && response.isSuccessful && response.body() != null) {
                    val remoto = response.body()!!
                    val localCantidad = carritoManager.getCantidadTotal()
                    val cantidadRemota = remoto.items.sumOf { it.cantidad }
                    val cantidad = if (cantidadRemota == 0 && localCantidad > 0) localCantidad else cantidadRemota
                    binding.tvCarritoBadge.text = cantidad.toString()
                    binding.tvCarritoBadge.visibility = if (cantidad > 0) View.VISIBLE else View.GONE
                } else {
                    val cantidad = carritoManager.getCantidadTotal()
                    binding.tvCarritoBadge.text = cantidad.toString()
                    binding.tvCarritoBadge.visibility = if (cantidad > 0) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {
                val cantidad = carritoManager.getCantidadTotal()
                binding.tvCarritoBadge.text = cantidad.toString()
                binding.tvCarritoBadge.visibility = if (cantidad > 0) View.VISIBLE else View.GONE
            }
        }
    }

    private fun agregarAlCarrito(producto: Producto) {
        if (isAddToCartInProgress) return
        android.util.Log.i("ProductListActivity", "agregarAlCarrito productoId=${producto.id}")
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        if (producto.id <= 0) {
            Toast.makeText(this, "Producto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        isAddToCartInProgress = true
        binding.fabCarrito.isEnabled = false
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                if (token.isBlank()) {
                    Toast.makeText(this@ProductListActivity, "Sesión inválida, vuelve a iniciar sesión", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProductListActivity, LoginActivity::class.java))
                    return@launch
                }
                val response = withContext(Dispatchers.IO) {
                    withRetry {
                        RetrofitClient.instance.agregarProductoCarrito(
                            token = "Bearer $token",
                            request = com.example.ejemploprueba.Model.CarritoAgregarRequest(
                                productoId = producto.id,
                                cantidad = 1
                            )
                        )
                    }
                }
                if (response.isSuccessful) {
                    android.util.Log.i("ProductListActivity", "Agregar remoto OK productoId=${producto.id}")
                    android.util.Log.i("ProductListActivity", "Agregar carrito OK")
                    updateCarritoBadge()
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "${producto.nombre} agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .setAction("Ver") { startActivity(Intent(this@ProductListActivity, CarritoActivity::class.java)) }
                        .show()
                } else {
                    val err = response.errorBody()?.string()?.take(200)
                    android.util.Log.e("ProductListActivity", "Agregar carrito error: ${response.code()} ${err}")
                    val item = CarritoItem(
                        detalleId = producto.id,
                        productoId = producto.id,
                        nombre = producto.nombre,
                        precio = producto.precio,
                        cantidad = 1,
                        imagen = producto.imagen
                    )
                    carritoManager.agregarProducto(item)
                    android.util.Log.i("ProductListActivity", "Agregar local fallback productoId=${producto.id}")
                    updateCarritoBadge()
                    com.google.android.material.snackbar.Snackbar.make(binding.root, "${producto.nombre} agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .setAction("Ver") { startActivity(Intent(this@ProductListActivity, CarritoActivity::class.java)) }
                        .show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductListActivity", "Agregar carrito exception", e)
                val item = CarritoItem(
                    detalleId = producto.id,
                    productoId = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    cantidad = 1,
                    imagen = producto.imagen
                )
                carritoManager.agregarProducto(item)
                android.util.Log.i("ProductListActivity", "Agregar local por excepción productoId=${producto.id}")
                updateCarritoBadge()
                com.google.android.material.snackbar.Snackbar.make(binding.root, "${producto.nombre} agregado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                    .setAction("Ver") { startActivity(Intent(this@ProductListActivity, CarritoActivity::class.java)) }
                    .show()
            } finally {
                isAddToCartInProgress = false
                binding.fabCarrito.isEnabled = true
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

    private fun showProductDetail(producto: Producto) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("producto_id", producto.id)
        intent.putExtra("nombre", producto.nombre)
        intent.putExtra("descripcion", producto.descripcion)
        intent.putExtra("precio", producto.precio)
        intent.putExtra("stock", producto.stock)
        intent.putExtra("categoria", producto.categoria)
        intent.putExtra("imagen", producto.imagen)
        startActivity(intent)
    }
    private fun setupInfiniteScroll() {
        binding.rvProducts.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val visible = lm.childCount
                val total = lm.itemCount
                val firstVisible = lm.findFirstVisibleItemPosition()
                val nearEnd = firstVisible + visible >= total - 4
                if (nearEnd && hasMore && !isLoading) cargarPagina(reset = false)
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            getColor(com.example.ejemploprueba.R.color.pastel_chocolate),
            getColor(com.example.ejemploprueba.R.color.pastel_pink)
        )
        binding.swipeRefresh.setOnRefreshListener {
            cargarPagina(reset = true)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.ejemploprueba.R.id.nav_catalogo -> true
                com.example.ejemploprueba.R.id.nav_carrito -> { startActivity(Intent(this, CarritoActivity::class.java)); true }
                com.example.ejemploprueba.R.id.nav_perfil -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun mostrarSelectorEntorno() {
        val opciones = arrayOf(
            "Dev Emulador",
            "Dev Dispositivo",
            "Stage",
            "Prod",
            "Usar por defecto"
        )
        val urls = arrayOf(
            "http://10.0.2.2:8080/",
            "http://192.168.1.100:8080/",
            "https://stage.api.gustitosabroson.com/",
            "https://api.gustitosabroson.com/",
            null
        )
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selecciona entorno")
            .setItems(opciones) { _, idx ->
                val url = urls[idx]
                SessionManager(this).setBaseUrlOverride(url)
                RetrofitClient.setBaseUrlOverride(url)
                Toast.makeText(this, url?.let { "Entorno: $it" } ?: "Entorno predeterminado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun cargarPagina(reset: Boolean) {
        if (reset) {
            page = 0
            hasMore = true
            allProducts = emptyList()
            adapter.submitList(allProducts)
        }
        isLoading = true
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getProductosDisponiblesPage(page, pageSize)
                if (resp.isSuccessful && resp.body() != null) {
                    val pageDto = resp.body()!!
                    val nuevos = pageDto.content.map { it.toLocal() }
                    allProducts = allProducts + nuevos
                    adapter.submitList(allProducts)
                    hasMore = pageDto.number < pageDto.totalPages - 1
                    if (hasMore) page += 1
                }
            } finally {
                isLoading = false
            }
        }
    }
    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CART = 2001
        val ID_PROFILE = 2002
        val ID_ADMIN = 2003
        val ID_CONTACT = 2004
        menu.clear()
        menu.add(Menu.NONE, ID_CART, Menu.NONE, "Carrito").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(Menu.NONE, ID_PROFILE, Menu.NONE, "Cuenta").setIcon(android.R.drawable.ic_menu_myplaces)
        val role = sessionManager.getUserRole() ?: "Cliente"
        if (role == "Administrador") {
            menu.add(Menu.NONE, ID_ADMIN, Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        } else {
            menu.add(Menu.NONE, ID_CONTACT, Menu.NONE, "Contactar").setIcon(android.R.drawable.ic_dialog_email)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_CART -> { startActivity(Intent(this, CarritoActivity::class.java)); overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) }
                ID_PROFILE -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) }
                ID_ADMIN -> { startActivity(Intent(this, AdminActivity::class.java)); overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right) }
                ID_CONTACT -> contactarNegocio()
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

}
