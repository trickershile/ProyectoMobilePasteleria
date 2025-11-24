package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.RegisterRequest
import com.example.ejemploprueba.databinding.ActivityRegisterBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.i("RegisterActivity", "onCreate")
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        android.util.Log.i("RegisterActivity", "setContentView done")

        sessionManager = SessionManager(this)

        binding.btnCrearCuenta.setOnClickListener {
            android.util.Log.i("RegisterActivity", "Crear cuenta clicked")
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegister(nombre, email, password)
        }

        binding.btnIrLogin.setOnClickListener {
            android.util.Log.i("RegisterActivity", "Ir a login clicked")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegister(nombre: String, email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                android.util.Log.i("RegisterActivity", "performRegister request")
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.register(RegisterRequest(nombre, email, password))
                }
                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    val userName = auth.nombre.ifBlank { nombre }
                    sessionManager.saveAuth(
                        token = auth.token,
                        userId = auth.usuarioId,
                        userName = userName,
                        userEmail = auth.email,
                        userRole = auth.rol
                    )
                    if (auth.rol.equals("Cliente", true)) {
                        try {
                            withContext(Dispatchers.IO) {
                                RetrofitClient.instance.crearClienteParaUsuarioAdmin(
                                    token = "Bearer ${auth.token}",
                                    usuarioId = auth.usuarioId
                                )
                            }
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(this@RegisterActivity, "Cuenta creada", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al registrar"
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        android.util.Log.d("RegisterActivity", "showLoading=$loading")
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCrearCuenta.isEnabled = !loading
        binding.etNombre.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }

    private fun navigateToMain() {
        val role = sessionManager.getUserRole() ?: "Cliente"
        val dest = if (role.equals("ADMIN", true) || role.equals("Administrador", true)) {
            AdminActivity::class.java
        } else {
            ProductListActivity::class.java
        }
        android.util.Log.i("RegisterActivity", "navigateToMain role=$role dest=${dest.simpleName}")
        startActivity(Intent(this, dest).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
