package com.example.ejemploprueba.Ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.ProductoRequestDTO
import com.example.ejemploprueba.Model.CategoriaResponseDTO
import com.example.ejemploprueba.Model.CrearInventarioDTO
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.ejemploprueba.databinding.ActivityAddProductBinding
import java.io.File
import java.io.FileOutputStream

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var sessionManager: SessionManager
    private var imageUrl: String = ""
    private var selectedImages: List<Uri> = emptyList()
    private var categorias: List<CategoriaResponseDTO> = emptyList()

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val path = saveImageToInternalStorage(it)
                imageUrl = path
                Glide.with(this@AddProductActivity).load(imageUrl).into(binding.ivProductImage)
            }
        }
    }

    private val selectMultipleImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImages = uris ?: emptyList()
        if (selectedImages.isNotEmpty()) {
            // preview first image
            Glide.with(this).load(selectedImages.first()).into(binding.ivProductImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        cargarCategorias()
        // Botón para seleccionar desde galería
        binding.btnSelectImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.btnSelectMultiple.setOnClickListener {
            selectMultipleImages.launch("image/*")
        }

        // Botón para cargar desde URL
        binding.btnLoadImage.setOnClickListener {
            val url = binding.etImageUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                imageUrl = url
                Glide.with(this).load(url).into(binding.ivProductImage)
            } else {
                Toast.makeText(this, "Ingresa una URL válida", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveProduct.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val descripcion = binding.etDescription.text.toString().trim()
            val precioStr = binding.etPrice.text.toString().trim()
            val precio = precioStr.toDoubleOrNull() ?: 0.0
            val stockInicial = binding.etStockInicial.text.toString().trim().toIntOrNull()
            val selectedIndex = binding.spCategoria.selectedItemPosition
            val categoriaId = if (selectedIndex >= 0 && selectedIndex < categorias.size) categorias[selectedIndex].id else null

            if (nombre.isEmpty() || precio <= 0) {
                Toast.makeText(this, "Completa nombre y precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (stockInicial == null || stockInicial < 0) {
                Toast.makeText(this, "Stock inicial inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoriaId == null) {
                Toast.makeText(this, "Selecciona categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarProducto(nombre, descripcion, precioStr, stockInicial, categoriaId)
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = "product_${System.currentTimeMillis()}.jpg"
                val file = File(filesDir, fileName)
                FileOutputStream(file).use { output ->
                    inputStream?.copyTo(output)
                }
                file.absolutePath
            } catch (e: Exception) {
                "https://via.placeholder.com/400"
            }
        }
    }

    private fun guardarProducto(nombre: String, descripcion: String, precio: String, stockInicial: Int, categoriaId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProduct.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val isLocalImage = imageUrl.isNotEmpty() && !imageUrl.startsWith("http")
                val response = withContext(Dispatchers.IO) {
                if (isLocalImage) {
                    val file = File(imageUrl)
                    val imagePart = MultipartBody.Part.createFormData(
                        "imagen",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    val parts = mapOf(
                        "nombre" to nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "descripcion" to descripcion.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "precio" to precio.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "categoriaId" to categoriaId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        "stock" to stockInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                    RetrofitClient.instance.crearProductoConImagen(
                        token = "Bearer $token",
                        body = parts,
                        imagen = imagePart
                    )
                } else {
                        val body = ProductoRequestDTO(
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            categoriaId = categoriaId,
                            stock = stockInicial
                        )
                        RetrofitClient.instance.crearProducto(
                            token = "Bearer $token",
                            body = body
                        )
                    }
                }

                if (response.isSuccessful) {
                    val creado = response.body()
                    if (creado != null) {
                        val invResp = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.crearInventario(
                                token = "Bearer $token",
                                body = CrearInventarioDTO(
                                    productoId = creado.id,
                                    cantidadInicial = stockInicial,
                                    stockMinimo = stockInicial
                                )
                            )
                        }
                        if (invResp.isSuccessful) {
                            if (selectedImages.size > 1) {
                                for (u in selectedImages.drop(1)) {
                                    try {
                                        val path = saveImageToInternalStorage(u)
                                        val file = File(path)
                                        val part = MultipartBody.Part.createFormData("imagen", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                                        RetrofitClient.instance.uploadImagen("Bearer $token", part)
                                    } catch (_: Exception) {}
                                }
                            }
                            Toast.makeText(this@AddProductActivity, "Producto e inventario guardados", Toast.LENGTH_SHORT).show()
                        } else {
                            val delResp = withContext(Dispatchers.IO) {
                                RetrofitClient.instance.eliminarProducto(
                                    token = "Bearer $token",
                                    id = creado.id
                                )
                            }
                            if (delResp.isSuccessful) {
                                Toast.makeText(this@AddProductActivity, "Error al crear inventario, producto revertido", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@AddProductActivity, "Error al crear inventario y al revertir producto", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@AddProductActivity, "Producto guardado", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                } else {
                    Toast.makeText(this@AddProductActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddProductActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProduct.isEnabled = true
            }
        }
    }

    private fun cargarCategorias() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { RetrofitClient.instance.getCategoriasActivas() }
                if (resp.isSuccessful && resp.body() != null) {
                    categorias = resp.body()!!
                    val nombres = categorias.map { it.nombre }
                    val adapter = android.widget.ArrayAdapter(this@AddProductActivity, android.R.layout.simple_spinner_dropdown_item, nombres)
                    binding.spCategoria.adapter = adapter
                } else {
                    categorias = emptyList()
                }
            } catch (_: Exception) {
                categorias = emptyList()
            }
        }
    }
}