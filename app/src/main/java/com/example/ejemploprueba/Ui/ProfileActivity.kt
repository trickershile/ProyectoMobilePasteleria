package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import com.example.ejemploprueba.API.RetrofitClient
import kotlinx.coroutines.launch
import com.example.ejemploprueba.databinding.ActivityProfileBinding
import com.example.ejemploprueba.utils.SessionManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private var perfilActual: com.example.ejemploprueba.Model.ClientePerfilDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.i("ProfileActivity", "onCreate")
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        android.util.Log.i("ProfileActivity", "setContentView done")

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

        loadUserData()
        cargarPerfilRemoto()

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            navigateToLogin()
        }

        binding.btnEditarPerfil.setOnClickListener {
            mostrarDialogoEditarPerfil()
        }

        binding.btnMisPedidos.setOnClickListener {
            startActivity(Intent(this, MisOrdenesActivity::class.java))
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_CATALOGO = 6001
        val ID_CARRITO = 6002
        val ID_CUENTA = 6003
        val ID_ADMIN = 6004
        val ID_CONTACT = 6005
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
                ID_CUENTA -> {}
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

    private fun mostrarDialogoEditarPerfil() {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val etNombre = android.widget.EditText(this).apply { hint = "Nombre"; setText(binding.tvUserName.text) }
        val etEmail = android.widget.EditText(this).apply { hint = "Email"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; setText(binding.tvUserEmail.text) }
        container.addView(etNombre)
        container.addView(etEmail)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar Perfil")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (nombre.isEmpty() || email.isEmpty()) {
                    android.widget.Toast.makeText(this, "Completa nombre y email", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    actualizarPerfil(nombre, email)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarPerfil(nombre: String, email: String) {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val id = perfilActual?.id ?: 0
                val resp = RetrofitClient.instance.actualizarClientePerfil(
                    "Bearer $token",
                    com.example.ejemploprueba.Model.ClientePerfilDTO(
                        id = id,
                        nombre = nombre,
                        email = email
                    )
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val perfil = resp.body()!!
                    perfilActual = perfil
                    binding.tvUserName.text = perfil.nombre
                    binding.tvUserEmail.text = perfil.email
                    android.widget.Toast.makeText(this@ProfileActivity, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this@ProfileActivity, "No se pudo actualizar", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@ProfileActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        binding.tvUserName.text = sessionManager.getUserName() ?: "Usuario"
        binding.tvUserEmail.text = sessionManager.getUserEmail() ?: "email@example.com"

        val role = sessionManager.getUserRole() ?: "Cliente"
        binding.tvUserRole.text = role

        if (role == "Administrador") {
            binding.tvUserRole.setBackgroundColor(0xFFE8F5E9.toInt())
            binding.tvUserRole.setTextColor(0xFF4CAF50.toInt())
        } else {
            binding.tvUserRole.setBackgroundColor(0xFFE3F2FD.toInt())
            binding.tvUserRole.setTextColor(0xFF2196F3.toInt())
        }
    }

    private fun cargarPerfilRemoto() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getClientePerfil("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val perfil = response.body()!!
                    perfilActual = perfil
                    binding.tvUserName.text = perfil.nombre
                    binding.tvUserEmail.text = perfil.email
                    android.util.Log.i("ProfileActivity", "Perfil cargado")
                }
            } catch (_: Exception) {
                android.util.Log.e("ProfileActivity", "Error al cargar perfil")
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}