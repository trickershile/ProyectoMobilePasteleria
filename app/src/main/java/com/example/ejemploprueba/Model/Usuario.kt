package com.example.ejemploprueba.Model

// Usuario
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val role: String = "Cliente",
    val bloqueado: Boolean = false
)

// Login
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class AuthResponseDTO(
    val token: String,
    val usuarioId: Int,
    val nombre: String,
    val email: String,
    val rol: String = "Cliente"
)

data class UsuarioCreadoDTO(
    val id: Int,
    val email: String
)

// Carrito
data class CarritoItem(
    val detalleId: Int = 0,
    val productoId: Int,
    val nombre: String,
    val precio: String,
    val cantidad: Int,
    val imagen: String
)

// Orden/Pago
data class Orden(
    val id: Int = 0,
    val usuarioId: Int,
    val items: List<CarritoItem>,
    val total: String,
    val estado: String = "Pendiente",
    val direccion: String,
    val fecha: String
)

data class OrdenRequest(
    val usuarioId: Int,
    val items: List<CarritoItem>,
    val total: String,
    val direccion: String
)

data class ClientePerfilDTO(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null
)

data class CarritoDTO(
    val id: Int,
    val items: List<CarritoItem>,
    val total: String
)

data class CarritoAgregarRequest(
    val productoId: Int,
    val cantidad: Int
)

data class PedidoDTO(
    val id: Int,
    val estado: String,
    val total: String,
    val fecha: String
)

data class PagoDTO(
    val id: Int,
    val pedidoId: Int,
    val estado: String,
    val monto: String
)

data class ProductoResponseDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val imagen: String? = null,
    val categoria: String? = null,
    val stock: Int? = null
)

fun ProductoResponseDTO.toLocal(): Producto =
    Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock ?: 0,
        categoria = categoria ?: "",
        imagen = imagen ?: ""
    )

data class ProductoRequestDTO(
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val categoriaId: Int? = null,
    val stock: Int? = null
)
data class ProductoUpdateRequestDTO(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: String? = null,
    val categoriaId: Int? = null,
    val stock: Int? = null
)

data class CategoriaResponseDTO(
    val id: Int,
    val nombre: String,
    val activa: Boolean
)

data class CategoriaRequestDTO(
    val nombre: String
)

data class UploadImagenResponseDTO(
    val filename: String,
    val url: String
)

data class MessageResponseDTO(
    val message: String
)

data class DetallePedidoDTO(
    val id: Int,
    val pedidoId: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: String,
    val total: String
)

data class CrearPagoDTO(
    val pedidoId: Int,
    val monto: String,
    val metodo: String
)

data class EnvioDTO(
    val id: Int,
    val pedidoId: Int,
    val estado: String,
    val empresa: String? = null,
    val numeroSeguimiento: String? = null,
    val fechaEstimada: String? = null
)

data class CrearEnvioDTO(
    val pedidoId: Int,
    val empresa: String? = null
)

data class ActualizarEstadoEnvioDTO(
    val estado: String
)

data class AsignarEmpresaDTO(
    val empresa: String
)

data class ActualizarFechaEnvioDTO(
    val fechaEstimada: String
)

data class CrearInventarioDTO(
    val productoId: Int,
    val cantidadInicial: Int,
    val stockMinimo: Int
)

data class ActualizarStockDTO(
    val cantidad: Int
)

data class AgregarStockDTO(
    val cantidad: Int
)

data class ReducirStockDTO(
    val cantidad: Int
)

data class StockMinimoDTO(
    val stockMinimo: Int
)

data class InventarioItemDTO(
    val productoId: Int,
    val nombre: String,
    val stock: Int,
    val stockMinimo: Int
)

data class DisponibilidadResponseDTO(
    val disponible: Boolean,
    val disponibleCantidad: Int
)

data class DashboardResumenDTO(
    val totalIngresos: String,
    val totalPedidos: Int,
    val totalClientes: Int
)

data class TopProductoDTO(
    val productoId: Int,
    val nombre: String,
    val cantidadVendida: Int,
    val ingresos: String
)

data class SerieIngresoDTO(
    val fecha: String,
    val monto: String
)

data class IngresoCategoriaDTO(
    val categoria: String,
    val ingresos: String
)

data class AOVDTO(
    val promedio: String
)

data class PagosMetodoDTO(
    val metodo: String,
    val total: String
)

data class BajoStockEventDTO(
    val productoId: Int,
    val nombre: String,
    val stock: Int,
    val stockMinimo: Int
)

// Paginación
data class PageDTO<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Int,
    val number: Int,
    val size: Int
)