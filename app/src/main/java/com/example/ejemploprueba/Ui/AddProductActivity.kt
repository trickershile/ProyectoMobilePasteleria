package com.example.ejemploprueba.Ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.text.InputType
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
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
    private val formViewModel: ProductFormViewModel by viewModels()

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                formViewModel.setImageUri(it)
                Glide.with(this@AddProductActivity)
                    .load(it)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivProductImage)
            }
        }
    }

    private val selectMultipleImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImages = uris ?: emptyList()
        if (selectedImages.isNotEmpty()) {
            // preview first image
            val first = selectedImages.first()
            formViewModel.setImageUri(first)
            Glide.with(this)
                .load(first)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        binding.etDescription.inputType = binding.etDescription.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        binding.etName.inputType = binding.etName.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
            val lower = url.lowercase()
            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                imageUrl = url
                formViewModel.setImageUrl(url)
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
            formViewModel.setNombre(binding.etName.text.toString().trim())
            formViewModel.setDescripcion(binding.etDescription.text.toString().trim())
            formViewModel.setPrecio(binding.etPrice.text.toString().trim())
            formViewModel.setCantidadInicial(binding.etStockInicial.text.toString().trim())

            val nombre = formViewModel.nombre.value?.trim().orEmpty()
            val precioDouble = formViewModel.getPrecioDouble() ?: 0.0
            val cantidadInicial = formViewModel.getCantidadInicialInt()
            val selectedIndex = binding.spCategoria.selectedItemPosition
            val categoriaId = if (selectedIndex >= 0 && selectedIndex < categorias.size) categorias[selectedIndex].id else null

            if (nombre.isEmpty() || precioDouble <= 0.0) {
                Toast.makeText(this, "Completa nombre y precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cantidadInicial == null || cantidadInicial <= 0) {
                Toast.makeText(this, "Stock inicial debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoriaId == null) {
                Toast.makeText(this, "Selecciona categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUri = formViewModel.imageUri.value
            val currentUrl = formViewModel.imageUrl.value
            if (currentUri == null && (currentUrl.isNullOrEmpty() || !currentUrl.startsWith("http"))) {
                Toast.makeText(this, "Selecciona una imagen o ingresa URL válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarProducto(nombre, formViewModel.descripcion.value?.trim().orEmpty(), String.format(java.util.Locale.US, "%.2f", precioDouble), cantidadInicial, categoriaId)
        }

        binding.etName.doOnTextChanged { text, _, _, _ -> formViewModel.setNombre(text?.toString().orEmpty()) }
        binding.etDescription.doOnTextChanged { text, _, _, _ -> formViewModel.setDescripcion(text?.toString().orEmpty()) }
        binding.etPrice.doOnTextChanged { text, _, _, _ -> formViewModel.setPrecio(text?.toString().orEmpty()) }
        binding.etStockInicial.doOnTextChanged { text, _, _, _ -> formViewModel.setCantidadInicial(text?.toString().orEmpty()) }

        if (formViewModel.nombre.value?.isNotEmpty() == true) binding.etName.setText(formViewModel.nombre.value)
        if (formViewModel.descripcion.value?.isNotEmpty() == true) binding.etDescription.setText(formViewModel.descripcion.value)
        if (formViewModel.precio.value?.isNotEmpty() == true) binding.etPrice.setText(formViewModel.precio.value)
        if (formViewModel.cantidadInicial.value?.isNotEmpty() == true) binding.etStockInicial.setText(formViewModel.cantidadInicial.value)
        if (!formViewModel.imageUrl.value.isNullOrEmpty()) {
            Glide.with(this)
                .load(formViewModel.imageUrl.value)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)
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
                val currentUri = formViewModel.imageUri.value
                val currentUrl = formViewModel.imageUrl.value ?: imageUrl
                val response = withContext(Dispatchers.IO) {
                if (currentUri != null) {
                    val input = contentResolver.openInputStream(currentUri)
                    val bytes = input?.readBytes() ?: throw IllegalArgumentException("Imagen inválida")
                    val imagePart = MultipartBody.Part.createFormData(
                        "imagen",
                        "imagen.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    val parts = mapOf(
                        "nombre" to nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "descripcion" to descripcion.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "precio" to precio.toRequestBody("text/plain".toMediaTypeOrNull()),
                        "categoriaId" to categoriaId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        "cantidadInicial" to stockInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                    RetrofitClient.instance.crearProductoConImagen(
                        token = "Bearer $token",
                        body = parts,
                        imagen = imagePart
                    )
                } else {
                        val precioNormalized = precio.replace(',', '.')
                        val body = ProductoRequestDTO(
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precioNormalized,
                            cantidadInicial = stockInicial,
                            categoriaId = categoriaId,
                            urlImagen = if (!currentUrl.isNullOrEmpty() && currentUrl.startsWith("http")) currentUrl else null
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
                        if (selectedImages.size > 1) {
                                for (u in selectedImages.drop(1)) {
                                    try {
                                        val input = contentResolver.openInputStream(u)
                                        val bytes = input?.readBytes() ?: continue
                                        val part = MultipartBody.Part.createFormData(
                                            "imagen",
                                            "extra.jpg",
                                            bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                        )
                                        RetrofitClient.instance.uploadImagen("Bearer $token", part)
                                    } catch (_: Exception) {}
                                }
                            }
                        Toast.makeText(this@AddProductActivity, "Producto guardado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@AddProductActivity, "Producto guardado", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                } else {
                    val parsed = com.example.ejemploprueba.API.parseApiError(response.errorBody())
                    val msg = parsed?.mensaje ?: "Error al guardar"
                    Toast.makeText(this@AddProductActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddProductActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProduct.isEnabled = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        com.example.ejemploprueba.utils.KeyboardUtils.hideIme(binding.root)
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
