package com.example.ejemploprueba.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.LoginRequest
import com.example.ejemploprueba.databinding.ActivityLoginBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.i("LoginActivity", "onCreate")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        android.util.Log.i("LoginActivity", "setContentView done")

        sessionManager = SessionManager(this)
        RetrofitClient.setBaseUrlOverride(sessionManager.getBaseUrlOverride())
        android.util.Log.i("LoginActivity", "SessionManager initialized")

        if (sessionManager.isLoggedIn()) {
            android.util.Log.i("LoginActivity", "User already logged in")
            navigateToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            android.util.Log.i("LoginActivity", "Login button clicked")
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        binding.btnRegister.setOnClickListener {
            android.util.Log.i("LoginActivity", "Register link clicked")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                android.util.Log.i("LoginActivity", "performLogin request")
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.login(LoginRequest(email, password))
                }

                // LOG PARA DEBUGGING
                android.util.Log.d("LoginActivity", "Response Code: ${response.code()}")
                android.util.Log.d("LoginActivity", "Response Body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    val userName = auth.nombre.ifBlank { email.substringBefore("@") }
                    sessionManager.saveAuth(
                        token = auth.token,
                        userId = auth.usuarioId,
                        userName = userName,
                        userEmail = auth.email,
                        userRole = auth.rol
                    )

                    Toast.makeText(this@LoginActivity, "¡Bienvenido $userName!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("LoginActivity", "Error Body: $errorBody")
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas - Code: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Exception: ", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        android.util.Log.d("LoginActivity", "showLoading=$loading")
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }

    private fun navigateToMain() {
        val role = sessionManager.getUserRole() ?: "Cliente"
        val dest = if (role.equals("ADMIN", true) || role.equals("Administrador", true)) {
            AdminActivity::class.java
        } else {
            ProductListActivity::class.java
        }
        android.util.Log.i("LoginActivity", "navigateToMain role=$role dest=${dest.simpleName}")
        startActivity(Intent(this, dest).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
