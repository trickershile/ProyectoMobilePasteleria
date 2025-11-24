package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.Usuario
import com.example.ejemploprueba.databinding.ActivityAdminUsuariosBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminUsuariosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUsuariosBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: UsuariosAdapter
    private var usuariosAll: List<Usuario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        cargarUsuarios()

        binding.fabAddUser.setOnClickListener { mostrarDialogoCrear() }
    }

    private fun setupRecyclerView() {
        adapter = UsuariosAdapter(
            onBlock = { usuario -> confirmarActivacion(usuario) },
            onCrearCliente = { usuario -> crearClienteParaUsuario(usuario) },
            onEditar = { usuario -> mostrarDialogoEditar(usuario) },
            onEliminar = { usuario -> confirmarEliminar(usuario) }
        )

        binding.rvUsuarios.apply {
            adapter = this@AdminUsuariosActivity.adapter
            layoutManager = LinearLayoutManager(this@AdminUsuariosActivity)
        }

        binding.etSearchUsuarios.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (q.isEmpty()) usuariosAll else usuariosAll.filter {
                    it.nombre.lowercase().contains(q) || it.email.lowercase().contains(q)
                }
                adapter.submitList(filtered)
            }
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            com.example.ejemploprueba.R.string.app_name,
            com.example.ejemploprueba.R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupDrawerMenu()

        val menu = binding.toolbar.menu
        val ID_VER_CLIENTES = 9101
        val item = menu.add(android.view.Menu.NONE, ID_VER_CLIENTES, android.view.Menu.NONE, "Ver clientes")
        item.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        binding.toolbar.setOnMenuItemClickListener { i ->
            when (i.itemId) {
                ID_VER_CLIENTES -> { mostrarClientes(); true }
                else -> false
            }
        }
    }

    private fun setupDrawerMenu() {
        val menu = binding.navView.menu
        val ID_HOME = 3001
        val ID_PRODUCTOS = 3002
        val ID_ADMIN = 3003
        val ID_CUENTA = 3004
        menu.clear()
        menu.add(android.view.Menu.NONE, ID_HOME, android.view.Menu.NONE, "Inicio").setIcon(android.R.drawable.ic_menu_view)
        menu.add(android.view.Menu.NONE, ID_PRODUCTOS, android.view.Menu.NONE, "Productos").setIcon(android.R.drawable.ic_menu_sort_by_size)
        menu.add(android.view.Menu.NONE, ID_CUENTA, android.view.Menu.NONE, "Cuenta").setIcon(android.R.drawable.ic_menu_myplaces)
        menu.add(android.view.Menu.NONE, ID_ADMIN, android.view.Menu.NONE, "Panel Admin").setIcon(android.R.drawable.ic_menu_manage)
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                ID_HOME -> startActivity(android.content.Intent(this, ProductListActivity::class.java))
                ID_PRODUCTOS -> startActivity(android.content.Intent(this, AdminProductosActivity::class.java))
                ID_CUENTA -> startActivity(android.content.Intent(this, ProfileActivity::class.java))
                ID_ADMIN -> startActivity(android.content.Intent(this, AdminActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun cargarUsuarios() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getUsuariosAdminTodos("Bearer $token")

                if (response.isSuccessful) {
                    val usuarios = response.body() ?: emptyList()
                    usuariosAll = usuarios

                    if (usuarios.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.btnRetryUsuarios.visibility = View.VISIBLE
                        binding.rvUsuarios.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.btnRetryUsuarios.visibility = View.GONE
                        binding.rvUsuarios.visibility = View.VISIBLE
                        adapter.submitList(usuarios)
                    }
                } else {
                    Toast.makeText(
                        this@AdminUsuariosActivity,
                        "Error al cargar usuarios",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnRetryUsuarios.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminUsuariosActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnRetryUsuarios.visibility = View.VISIBLE
            } finally {
                showLoading(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnRetryUsuarios.setOnClickListener { cargarUsuarios() }
    }

    private fun confirmarActivacion(usuario: Usuario) {
        val bloqueadoActual = usuario.activo?.let { !it } ?: usuario.bloqueado
        val accion = if (bloqueadoActual) "activar" else "desactivar"

        AlertDialog.Builder(this)
            .setTitle("${accion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Usuario")
            .setMessage("¿Estás seguro de $accion a ${usuario.nombre}?")
            .setPositiveButton("Confirmar") { _, _ ->
                cambiarEstadoUsuario(usuario)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarEstadoUsuario(usuario: Usuario) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val bloqueadoActual = usuario.activo?.let { !it } ?: usuario.bloqueado
                val response = if (bloqueadoActual) {
                    RetrofitClient.instance.activarUsuarioAdmin(
                        token = "Bearer $token",
                        usuarioId = usuario.id
                    )
                } else {
                    RetrofitClient.instance.desactivarUsuarioAdmin(
                        token = "Bearer $token",
                        usuarioId = usuario.id
                    )
                }

                if (response.isSuccessful) {
                    val accion = if (bloqueadoActual) "activado" else "desactivado"
                    Toast.makeText(
                        this@AdminUsuariosActivity,
                        "Usuario $accion",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(
                        this@AdminUsuariosActivity,
                        "Error al procesar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminUsuariosActivity,
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

    private fun mostrarClientes() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = withContext(Dispatchers.IO) { RetrofitClient.instance.getClientesAdminTodos("Bearer $token") }
                if (resp.isSuccessful) {
                    val clientes = resp.body() ?: emptyList()
                    val nombres = clientes.map { "#${it.id} · ${it.nombre} · ${it.email}" }
                    val adapter = android.widget.ArrayAdapter(this@AdminUsuariosActivity, android.R.layout.simple_list_item_1, nombres)
                    val list = android.widget.ListView(this@AdminUsuariosActivity).apply { this.adapter = adapter }
                    androidx.appcompat.app.AlertDialog.Builder(this@AdminUsuariosActivity)
                        .setTitle("Clientes")
                        .setView(list)
                        .setPositiveButton("Ver por ID") { _, _ -> mostrarDialogoClientePorId() }
                        .setNegativeButton("Cerrar", null)
                        .show()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "Error al cargar clientes"
                    Toast.makeText(this@AdminUsuariosActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun mostrarDialogoClientePorId() {
        val input = android.widget.EditText(this).apply { hint = "Cliente ID"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Buscar cliente por ID")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val id = input.text.toString().toIntOrNull() ?: 0
                if (id > 0) obtenerClientePorId(id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun obtenerClientePorId(id: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = withContext(Dispatchers.IO) { RetrofitClient.instance.getClientePorIdAdmin("Bearer $token", id) }
                if (resp.isSuccessful && resp.body() != null) {
                    val c = resp.body()!!
                    val detalle = "ID: ${c.id}\nNombre: ${c.nombre}\nEmail: ${c.email}\nTeléfono: ${c.telefono ?: "-"}\nDirección: ${c.direccion ?: "-"}"
                    androidx.appcompat.app.AlertDialog.Builder(this@AdminUsuariosActivity)
                        .setTitle("Cliente #${c.id}")
                        .setMessage(detalle)
                        .setPositiveButton("Cerrar", null)
                        .show()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "No encontrado"
                    Toast.makeText(this@AdminUsuariosActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                showLoading(false)
            }
        }
    }

    private fun mostrarDialogoCrear() {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val etNombre = android.widget.EditText(this).apply { hint = "Nombre" }
        val etEmail = android.widget.EditText(this).apply { hint = "Email"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val roles = arrayOf("Cliente", "Administrador")
        val spRole = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@AdminUsuariosActivity, android.R.layout.simple_spinner_dropdown_item, roles)
        }
        container.addView(etNombre)
        container.addView(etEmail)
        container.addView(spRole)

        AlertDialog.Builder(this)
            .setTitle("Crear usuario")
            .setView(container)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val role = spRole.selectedItem.toString()
                if (nombre.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Completa nombre y email", Toast.LENGTH_SHORT).show()
                } else {
                    crearUsuario(nombre, email, role)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(usuario: Usuario) {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val etNombre = android.widget.EditText(this).apply { hint = "Nombre"; setText(usuario.nombre) }
        val etEmail = android.widget.EditText(this).apply { hint = "Email"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; setText(usuario.email) }
        val roles = arrayOf("Cliente", "Administrador")
        val spRole = android.widget.Spinner(this).apply {
            adapter = android.widget.ArrayAdapter(this@AdminUsuariosActivity, android.R.layout.simple_spinner_dropdown_item, roles)
            setSelection(if (usuario.role.equals("Administrador", true)) 1 else 0)
        }
        container.addView(etNombre)
        container.addView(etEmail)
        container.addView(spRole)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar usuario")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val role = spRole.selectedItem.toString()
                if (nombre.isEmpty() || email.isEmpty()) {
                    android.widget.Toast.makeText(this, "Completa nombre y email", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    val bloqueadoActual = usuario.activo?.let { !it } ?: usuario.bloqueado
                    editarUsuario(usuario.id, nombre, email, role, bloqueadoActual)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarUsuario(id: Int, nombre: String, email: String, role: String, bloqueado: Boolean) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.actualizarUsuarioAdmin(
                        token = "Bearer $token",
                        usuarioId = id,
                        usuario = Usuario(id = id, nombre = nombre, email = email, role = role, bloqueado = bloqueado, activo = !bloqueado)
                    )
                }
                if (resp.isSuccessful) {
                    android.widget.Toast.makeText(this@AdminUsuariosActivity, "Usuario actualizado", android.widget.Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "Error al actualizar"
                    android.widget.Toast.makeText(this@AdminUsuariosActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@AdminUsuariosActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun confirmarEliminar(usuario: Usuario) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Eliminar a ${usuario.nombre}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarUsuario(usuario.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarUsuario(id: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.eliminarUsuarioAdmin(
                        token = "Bearer $token",
                        usuarioId = id
                    )
                }
                if (resp.isSuccessful) {
                    android.widget.Toast.makeText(this@AdminUsuariosActivity, "Usuario eliminado", android.widget.Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    val code = resp.code()
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "No se pudo eliminar"
                    if (code == 409 || (msg.contains("relacion", true) || msg.contains("vincul", true))) {
                        val desResp = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.desactivarUsuarioAdmin(
                                token = "Bearer $token",
                                usuarioId = id
                            )
                        }
                        if (desResp.isSuccessful) {
                            android.widget.Toast.makeText(this@AdminUsuariosActivity, "Usuario desactivado (no se pudo eliminar)", android.widget.Toast.LENGTH_SHORT).show()
                            cargarUsuarios()
                        } else {
                            val p2 = com.example.ejemploprueba.API.parseApiError(desResp.errorBody())
                            val m2 = p2?.mensaje ?: msg
                            android.widget.Toast.makeText(this@AdminUsuariosActivity, m2, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(this@AdminUsuariosActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@AdminUsuariosActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun crearUsuario(nombre: String, email: String, role: String) {
        showLoading(true)
        binding.fabAddUser.isEnabled = false
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                    val resp = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.crearUsuarioAdmin(
                            token = "Bearer $token",
                            usuario = Usuario(id = 0, nombre = nombre, email = email, role = role, bloqueado = false, activo = true)
                        )
                    }
                if (resp.isSuccessful) {
                    val creado = resp.body()
                    if (creado != null && role.equals("Cliente", true)) {
                        val clienteResp = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.crearClienteParaUsuarioAdmin(
                                token = "Bearer $token",
                                usuarioId = creado.id
                            )
                        }
                        if (clienteResp.isSuccessful) {
                            Toast.makeText(this@AdminUsuariosActivity, "Usuario y cliente creados", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AdminUsuariosActivity, "Usuario creado, error al crear cliente", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AdminUsuariosActivity, "Usuario creado", Toast.LENGTH_SHORT).show()
                    }
                    cargarUsuarios()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(resp.errorBody())
                    val msg = parsed?.mensaje ?: "Error al crear"
                    Toast.makeText(this@AdminUsuariosActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUsuariosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
                binding.fabAddUser.isEnabled = true
            }
        }
    }
    private fun crearClienteParaUsuario(usuario: Usuario) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.crearClienteParaUsuarioAdmin(
                        token = "Bearer $token",
                        usuarioId = usuario.id
                    )
                }
                if (resp.isSuccessful) {
                    Toast.makeText(this@AdminUsuariosActivity, "Cliente creado para ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                } else {
                    val err = resp.errorBody()?.string()?.take(200)
                    Toast.makeText(this@AdminUsuariosActivity, err ?: "No se pudo crear cliente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUsuariosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
}
