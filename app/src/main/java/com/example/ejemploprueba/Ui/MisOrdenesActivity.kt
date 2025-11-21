package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.databinding.ActivityMisOrdenesBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class MisOrdenesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMisOrdenesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OrdenesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisOrdenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        cargarOrdenes()
    }

    private fun setupRecyclerView() {
        adapter = OrdenesAdapter()
        binding.rvOrdenes.apply {
            adapter = this@MisOrdenesActivity.adapter
            layoutManager = LinearLayoutManager(this@MisOrdenesActivity)
        }
    }

    private fun cargarOrdenes() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = RetrofitClient.instance.getMisPedidos(
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val ordenes = response.body() ?: emptyList()

                    if (ordenes.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvOrdenes.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvOrdenes.visibility = View.VISIBLE
                        adapter.submitList(ordenes)
                    }
                } else {
                    Toast.makeText(
                        this@MisOrdenesActivity,
                        "Error al cargar órdenes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MisOrdenesActivity,
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
}