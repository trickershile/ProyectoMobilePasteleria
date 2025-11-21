package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.ejemploprueba.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnGestionProductos.setOnClickListener {
            startActivity(Intent(this, AdminProductosActivity::class.java))
        }

        binding.btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }

        binding.btnGestionOrdenes.setOnClickListener {
            startActivity(Intent(this, AdminOrdenesActivity::class.java))
        }

        binding.btnGestionCategorias.setOnClickListener {
            startActivity(Intent(this, AdminCategoriasActivity::class.java))
        }

        binding.btnGestionInventario.setOnClickListener {
            startActivity(Intent(this, AdminInventarioActivity::class.java))
        }

        binding.btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, AdminEstadisticasActivity::class.java))
        }

        binding.btnGestionPagos.setOnClickListener {
            startActivity(Intent(this, AdminPagosActivity::class.java))
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 7001
        val ID_ADMIN = 7002
        val ID_USUARIOS = 7003
        val ID_PRODUCTOS = 7004
        val ID_ORDENES = 7005
        val ID_PAGOS = 7006
        val ID_INVENTARIO = 7007
        val ID_CATEGORIAS = 7008
        val ID_CUENTA = 7009
        menu.clear()
        menu.add(android.view.Menu.NONE, ID_CATALOGO, android.view.Menu.NONE, "Catálogo").setIcon(android.R.drawable.ic_menu_view)
        menu.add(android.view.Menu.NONE, ID_ADMIN, android.view.Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        menu.add(android.view.Menu.NONE, ID_USUARIOS, android.view.Menu.NONE, "Usuarios").setIcon(android.R.drawable.ic_menu_myplaces)
        menu.add(android.view.Menu.NONE, ID_PRODUCTOS, android.view.Menu.NONE, "Productos").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(android.view.Menu.NONE, ID_ORDENES, android.view.Menu.NONE, "Órdenes").setIcon(android.R.drawable.ic_menu_agenda)
        menu.add(android.view.Menu.NONE, ID_PAGOS, android.view.Menu.NONE, "Pagos").setIcon(android.R.drawable.ic_menu_info_details)
        menu.add(android.view.Menu.NONE, ID_INVENTARIO, android.view.Menu.NONE, "Inventario").setIcon(android.R.drawable.ic_menu_day)
        menu.add(android.view.Menu.NONE, ID_CATEGORIAS, android.view.Menu.NONE, "Categorías").setIcon(android.R.drawable.ic_menu_edit)
        menu.add(android.view.Menu.NONE, ID_CUENTA, android.view.Menu.NONE, "Cuenta").setIcon(android.R.drawable.ic_menu_myplaces)

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_CATALOGO -> startActivity(android.content.Intent(this, ProductListActivity::class.java))
                ID_ADMIN -> {}
                ID_USUARIOS -> startActivity(android.content.Intent(this, AdminUsuariosActivity::class.java))
                ID_PRODUCTOS -> startActivity(android.content.Intent(this, AdminProductosActivity::class.java))
                ID_ORDENES -> startActivity(android.content.Intent(this, AdminOrdenesActivity::class.java))
                ID_PAGOS -> startActivity(android.content.Intent(this, AdminPagosActivity::class.java))
                ID_INVENTARIO -> startActivity(android.content.Intent(this, AdminInventarioActivity::class.java))
                ID_CATEGORIAS -> startActivity(android.content.Intent(this, AdminCategoriasActivity::class.java))
                ID_CUENTA -> startActivity(android.content.Intent(this, ProfileActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }
}