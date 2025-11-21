package com.example.ejemploprueba.Ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.TopProductoDTO
import com.example.ejemploprueba.databinding.ActivityAdminEstadisticasBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch

class AdminEstadisticasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminEstadisticasBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var topAdapter: TopProductosAdapter
    private lateinit var ingresosAdapter: IngresosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupRecycler()
        cargarResumen()
        cargarTopProductos()
        setupIngresos()
    }

    private fun setupRecycler() {
        topAdapter = TopProductosAdapter()
        ingresosAdapter = IngresosAdapter()
        binding.rvTopProductos.apply {
            adapter = this@AdminEstadisticasActivity.topAdapter
            layoutManager = LinearLayoutManager(this@AdminEstadisticasActivity)
        }
        binding.rvIngresos.apply {
            adapter = this@AdminEstadisticasActivity.ingresosAdapter
            layoutManager = LinearLayoutManager(this@AdminEstadisticasActivity)
        }
    }

    private fun cargarResumen() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getDashboardResumen("Bearer $token")
                if (resp.isSuccessful && resp.body() != null) {
                    val d = resp.body()!!
                    binding.tvTotalIngresos.text = d.totalIngresos
                    binding.tvTotalPedidos.text = d.totalPedidos.toString()
                    binding.tvTotalClientes.text = d.totalClientes.toString()
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun cargarTopProductos() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val resp = RetrofitClient.instance.getTopProductos("Bearer $token", 5)
                if (resp.isSuccessful) {
                    topAdapter.submitList(resp.body() ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    private fun setupIngresos() {
        binding.btnBuscarIngresos.setOnClickListener {
            val desde = binding.etDesde.text.toString().trim()
            val hasta = binding.etHasta.text.toString().trim()
            if (desde.isEmpty() || hasta.isEmpty()) return@setOnClickListener
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val token = sessionManager.getToken() ?: ""
                    val resp = RetrofitClient.instance.getIngresosDiarios("Bearer $token", desde, hasta)
                    if (resp.isSuccessful) {
                    val series = resp.body() ?: emptyList()
                    ingresosAdapter.submitList(series)
                    } else {
                        Toast.makeText(this@AdminEstadisticasActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}