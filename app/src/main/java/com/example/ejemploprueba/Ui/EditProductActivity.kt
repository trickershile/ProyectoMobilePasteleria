package com.example.ejemploprueba.Ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.text.InputType
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ejemploprueba.API.RetrofitClient
import com.example.ejemploprueba.Model.ProductoResponseDTO
import com.example.ejemploprueba.Model.ProductoUpdateRequestDTO
import com.example.ejemploprueba.Model.CategoriaResponseDTO
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.ejemploprueba.databinding.ActivityEditProductBinding
import com.example.ejemploprueba.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    private lateinit var sessionManager: SessionManager
    private var productoId: Int = 0
    private var imageUrl: String = ""
    private var selectedImageUri: Uri? = null
    private var extraImages: List<Uri> = emptyList()
    private var producto: ProductoResponseDTO? = null
    private var categorias: List<CategoriaResponseDTO> = emptyList()

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        uri?.let {
            Glide.with(this@EditProductActivity)
                .load(it)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)
        }
    }

    private val selectMultipleImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        extraImages = uris ?: emptyList()
        if (extraImages.isNotEmpty()) {
            Glide.with(this)
                .load(extraImages.first())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        productoId = intent.getIntExtra("producto_id", 0)

        if (productoId == 0) {
            Toast.makeText(this, "Error: ID inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarProducto()
        cargarCategorias()
        setupListeners()
    }

    private fun cargarProducto() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProductoPorId(productoId)

                if (response.isSuccessful && response.body() != null) {
                    producto = response.body()
                    mostrarDatos(producto!!)
                } else {
                    Toast.makeText(
                        this@EditProductActivity,
                        "Error al cargar producto",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditProductActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun mostrarDatos(producto: ProductoResponseDTO) {
        binding.etName.setText(producto.nombre)
        binding.etDescription.setText(producto.descripcion)
        binding.etPrice.setText(producto.precio)
        binding.etStock.setText((producto.stock ?: 0).toString())

        imageUrl = producto.imagen ?: ""
        Glide.with(this)
            .load(producto.imagen?.takeIf { it.isNotBlank() } ?: android.R.drawable.ic_menu_report_image)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.ic_menu_report_image)
            .into(binding.ivProductImage)

        if (categorias.isNotEmpty()) {
            val nombres = categorias.map { it.nombre }
            val catName = producto.categoria ?: ""
            val idx = nombres.indexOfFirst { it.equals(catName, true) }
            if (idx >= 0) binding.spCategoria.setSelection(idx)
        }
        binding.spCategoria.isEnabled = categorias.isNotEmpty()
        binding.tvCategoriaAdvertencia.visibility = if (categorias.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun cargarCategorias() {
        lifecycleScope.launch {
            try {
                val resp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.instance.getCategoriasActivas()
                }
                if (resp.isSuccessful && resp.body() != null) {
                    categorias = resp.body()!!
                    val nombres = categorias.map { it.nombre }
                    val adapter = android.widget.ArrayAdapter(this@EditProductActivity, android.R.layout.simple_spinner_dropdown_item, nombres)
                    binding.spCategoria.adapter = adapter
                    val catName = producto?.categoria ?: ""
                    if (catName.isNotEmpty()) {
                        val idx = nombres.indexOfFirst { it.equals(catName, true) }
                        if (idx >= 0) binding.spCategoria.setSelection(idx)
                    }
                    binding.spCategoria.isEnabled = categorias.isNotEmpty()
                    binding.tvCategoriaAdvertencia.visibility = if (categorias.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    categorias = emptyList()
                    binding.spCategoria.isEnabled = false
                    binding.tvCategoriaAdvertencia.visibility = View.VISIBLE
                }
            } catch (_: Exception) {
                categorias = emptyList()
                binding.spCategoria.isEnabled = false
                binding.tvCategoriaAdvertencia.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.btnLoadExtraImages.setOnClickListener {
            selectMultipleImages.launch("image/*")
        }

        binding.btnLoadImage.setOnClickListener {
            val url = binding.etImageUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                imageUrl = url
                Glide.with(this)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivProductImage)
            } else {
                Toast.makeText(this, "Ingresa una URL válida", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveProduct.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val nombre = binding.etName.text.toString().trim()
        val descripcion = binding.etDescription.text.toString().trim()
        val precio = binding.etPrice.text.toString().trim()
        val stock = binding.etStock.text.toString().toIntOrNull() ?: 0
        val selectedIndex = binding.spCategoria.selectedItemPosition
        val categoriaId: Int? = if (selectedIndex >= 0 && selectedIndex < categorias.size) categorias[selectedIndex].id else null

        if (nombre.isEmpty() || precio.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.getToken() ?: ""
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    if (selectedImageUri != null) {
                        val input = contentResolver.openInputStream(selectedImageUri!!)
                        val bytes = input?.readBytes() ?: throw IllegalArgumentException("Imagen inválida")
                        val imagePart = MultipartBody.Part.createFormData(
                            "imagen",
                            "imagen.jpg",
                            bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                        val parts: MutableMap<String, okhttp3.RequestBody> = mutableMapOf(
                            "nombre" to nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                            "descripcion" to descripcion.toRequestBody("text/plain".toMediaTypeOrNull()),
                            "precio" to precio.toRequestBody("text/plain".toMediaTypeOrNull()),
                            "stock" to stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        )
                        if (categoriaId != null) {
                            parts["categoriaId"] = categoriaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        }
                        RetrofitClient.instance.actualizarProductoConImagen(
                            token = "Bearer $token",
                            id = productoId,
                            body = parts,
                            imagen = imagePart
                        )
                    } else {
                        val body = ProductoUpdateRequestDTO(
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            stock = stock,
                            categoriaId = categoriaId,
                            urlImagen = if (imageUrl.startsWith("http")) imageUrl else null
                        )
                        RetrofitClient.instance.actualizarProducto(
                            token = "Bearer $token",
                            id = productoId,
                            body = body
                        )
                    }
                }

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditProductActivity,
                        "Producto actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    // optional extra uploads
                    val uploadToken = sessionManager.getToken() ?: ""
                    for (u in extraImages) {
                        try {
                            val input = contentResolver.openInputStream(u)
                            val bytes = input?.readBytes() ?: continue
                            val part = MultipartBody.Part.createFormData(
                                "imagen",
                                "extra.jpg",
                                bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            )
                            RetrofitClient.instance.uploadImagen("Bearer $uploadToken", part)
                        } catch (_: Exception) {}
                    }
                    finish()
                } else {
                    Toast.makeText(
                        this@EditProductActivity,
                        "Error al actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditProductActivity,
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
        binding.btnSaveProduct.isEnabled = !loading
    }
}
