package com.example.ejemploprueba.API

import com.example.ejemploprueba.Model.*
import retrofit2.Response
import retrofit2.http.*

interface PasteleriaApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDTO>

    @POST("/api/auth/registro")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDTO>

    @GET("/api/usuarios/admin/todos")
    suspend fun getUsuariosAdminTodos(@Header("Authorization") token: String): Response<List<Usuario>>

    @POST("/api/usuarios/admin")
    suspend fun crearUsuarioAdmin(
        @Header("Authorization") token: String,
        @Body usuario: Usuario
    ): Response<UsuarioCreadoDTO>

    @PUT("/api/usuarios/admin/{usuarioId}")
    suspend fun actualizarUsuarioAdmin(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int,
        @Body usuario: Usuario
    ): Response<Usuario>

    @PATCH("/api/usuarios/admin/{usuarioId}/rol")
    suspend fun cambiarRolUsuario(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int,
        @Body body: Map<String, String>
    ): Response<Usuario>

    @DELETE("/api/usuarios/admin/{usuarioId}")
    suspend fun eliminarUsuarioAdmin(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int
    ): Response<Unit>

    @PUT("/api/usuarios/admin/{usuarioId}/desactivar")
    suspend fun desactivarUsuarioAdmin(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int
    ): Response<Usuario>

    @PUT("/api/usuarios/admin/{usuarioId}/activar")
    suspend fun activarUsuarioAdmin(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int
    ): Response<Usuario>

    @PATCH("/api/usuarios/password")
    suspend fun cambiarPassword(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Unit>

    @GET("/api/clientes/perfil")
    suspend fun getClientePerfil(@Header("Authorization") token: String): Response<ClientePerfilDTO>

    @PUT("/api/clientes/perfil")
    suspend fun actualizarClientePerfil(
        @Header("Authorization") token: String,
        @Body perfil: ClientePerfilDTO
    ): Response<ClientePerfilDTO>

    @GET("/api/clientes/admin/todos")
    suspend fun getClientesAdminTodos(@Header("Authorization") token: String): Response<List<ClientePerfilDTO>>

    @GET("/api/clientes/admin/{clienteId}")
    suspend fun getClientePorIdAdmin(
        @Header("Authorization") token: String,
        @Path("clienteId") clienteId: Int
    ): Response<ClientePerfilDTO>

    @POST("/api/clientes/admin/crear-para-usuario/{usuarioId}")
    suspend fun crearClienteParaUsuarioAdmin(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Int
    ): Response<ClientePerfilDTO>

    @GET("/api/carrito")
    suspend fun getCarrito(@Header("Authorization") token: String): Response<CarritoDTO>

    @POST("/api/carrito/agregar")
    suspend fun agregarProductoCarrito(
        @Header("Authorization") token: String,
        @Body request: CarritoAgregarRequest
    ): Response<CarritoDTO>

    @PUT("/api/carrito/detalle/{detalleId}")
    suspend fun actualizarCantidadCarrito(
        @Header("Authorization") token: String,
        @Path("detalleId") detalleId: Int,
        @Body body: Map<String, Int>
    ): Response<CarritoDTO>

    @DELETE("/api/carrito/detalle/{detalleId}")
    suspend fun eliminarDetalleCarrito(
        @Header("Authorization") token: String,
        @Path("detalleId") detalleId: Int
    ): Response<CarritoDTO>

    @DELETE("/api/carrito/vaciar")
    suspend fun vaciarCarrito(@Header("Authorization") token: String): Response<Unit>

    @POST("/api/pedidos/crear")
    suspend fun crearPedidoDesdeCarrito(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<PedidoDTO>

    @GET("/api/pedidos/mis-pedidos")
    suspend fun getMisPedidos(@Header("Authorization") token: String): Response<List<PedidoDTO>>

    @GET("/api/pedidos/admin/todos")
    suspend fun getPedidosAdminTodos(@Header("Authorization") token: String): Response<List<PedidoDTO>>

    @PUT("/api/pedidos/admin/{pedidoId}/estado")
    suspend fun actualizarEstadoPedidoAdmin(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Body body: Map<String, String>
    ): Response<PedidoDTO>

    @POST("/api/pagos/crear")
    suspend fun crearPago(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<PagoDTO>

    @GET("/api/pagos/pedido/{pedidoId}")
    suspend fun getPagoPorPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<PagoDTO>

    @GET("/api/pagos/{pagoId}")
    suspend fun getPagoPorId(
        @Header("Authorization") token: String,
        @Path("pagoId") pagoId: Int
    ): Response<PagoDTO>

    @PUT("/api/pagos/admin/{pagoId}/aprobar")
    suspend fun aprobarPagoAdmin(
        @Header("Authorization") token: String,
        @Path("pagoId") pagoId: Int
    ): Response<PagoDTO>

    @PUT("/api/pagos/admin/{pagoId}/rechazar")
    suspend fun rechazarPagoAdmin(
        @Header("Authorization") token: String,
        @Path("pagoId") pagoId: Int
    ): Response<PagoDTO>

    @PUT("/api/pagos/admin/{pagoId}/reembolsar")
    suspend fun reembolsarPagoAdmin(
        @Header("Authorization") token: String,
        @Path("pagoId") pagoId: Int
    ): Response<PagoDTO>

    @GET("/api/pagos/admin/estado/{estado}")
    suspend fun getPagosPorEstadoAdmin(
        @Header("Authorization") token: String,
        @Path("estado") estado: String
    ): Response<List<PagoDTO>>

    @GET("/api/productos")
    suspend fun getProductos(): Response<List<ProductoResponseDTO>>

    @GET("/api/productos/disponibles")
    suspend fun getProductosDisponibles(): Response<List<ProductoResponseDTO>>

    @GET("/api/productos/disponibles/page")
    suspend fun getProductosDisponiblesPage(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageDTO<ProductoResponseDTO>>

    @GET("/api/productos/buscar")
    suspend fun buscarProductos(
        @Query("nombre") nombre: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageDTO<ProductoResponseDTO>>

    @GET("/api/productos/{id}")
    suspend fun getProductoPorId(@Path("id") id: Int): Response<ProductoResponseDTO>

    @POST("/api/productos")
    suspend fun crearProducto(
        @Header("Authorization") token: String,
        @Body body: ProductoRequestDTO
    ): Response<ProductoResponseDTO>

    @PUT("/api/productos/{id}")
    suspend fun actualizarProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: ProductoUpdateRequestDTO
    ): Response<ProductoResponseDTO>

    @DELETE("/api/productos/{id}")
    suspend fun eliminarProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @Multipart
    @POST("/api/productos/con-imagen")
    suspend fun crearProductoConImagen(
        @Header("Authorization") token: String,
        @PartMap body: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part imagen: okhttp3.MultipartBody.Part
    ): Response<ProductoResponseDTO>

    @Multipart
    @PUT("/api/productos/{id}/con-imagen")
    suspend fun actualizarProductoConImagen(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @PartMap body: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part imagen: okhttp3.MultipartBody.Part
    ): Response<ProductoResponseDTO>

    @GET("/api/categorias")
    suspend fun getCategorias(): Response<List<CategoriaResponseDTO>>

    @GET("/api/categorias/activas")
    suspend fun getCategoriasActivas(): Response<List<CategoriaResponseDTO>>

    @GET("/api/categorias/{id}")
    suspend fun getCategoriaPorId(@Path("id") id: Int): Response<CategoriaResponseDTO>

    @POST("/api/categorias")
    suspend fun crearCategoria(
        @Header("Authorization") token: String,
        @Body body: CategoriaRequestDTO
    ): Response<CategoriaResponseDTO>

    @PUT("/api/categorias/{id}")
    suspend fun actualizarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: CategoriaRequestDTO
    ): Response<CategoriaResponseDTO>

    @DELETE("/api/categorias/{id}")
    suspend fun desactivarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("/api/detalle-pedido")
    suspend fun getDetallesPedidoAdmin(
        @Header("Authorization") token: String
    ): Response<List<DetallePedidoDTO>>

    @GET("/api/detalle-pedido/{id}")
    suspend fun getDetallePedidoPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DetallePedidoDTO>

    @GET("/api/detalle-pedido/pedido/{pedidoId}")
    suspend fun getDetallesPorPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<List<DetallePedidoDTO>>

    @POST("/api/detalle-pedido/pedido/{pedidoId}/producto/{productoId}")
    suspend fun crearDetallePedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Path("productoId") productoId: Int,
        @Query("cantidad") cantidad: Int
    ): Response<DetallePedidoDTO>

    @POST("/api/detalle-pedido/pedido/{pedidoId}/producto/{productoId}/con-precio")
    suspend fun crearDetallePedidoConPrecio(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Path("productoId") productoId: Int,
        @Query("cantidad") cantidad: Int,
        @Query("precioUnitario") precioUnitario: String
    ): Response<DetallePedidoDTO>

    @DELETE("/api/detalle-pedido/{id}")
    suspend fun eliminarDetallePedido(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("/api/detalle-pedido/pedido/{pedidoId}")
    suspend fun eliminarDetallesPorPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<Unit>

    @GET("/api/detalle-pedido/pedido/{pedidoId}/total")
    suspend fun getTotalPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<MessageResponseDTO>

    @GET("/api/detalle-pedido/pedido/{pedidoId}/items")
    suspend fun getItemsPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<MessageResponseDTO>

    @GET("/api/detalle-pedido/producto/{productoId}")
    suspend fun getDetallesPorProducto(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int
    ): Response<List<DetallePedidoDTO>>

    @GET("/api/detalle-pedido/producto/{productoId}/cantidad-vendida")
    suspend fun getCantidadVendida(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int
    ): Response<MessageResponseDTO>

    @GET("/api/detalle-pedido/pedido/{pedidoId}/producto/{productoId}/existe")
    suspend fun existeProductoEnPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Path("productoId") productoId: Int
    ): Response<MessageResponseDTO>

    @POST("/api/imagenes/upload")
    @Multipart
    suspend fun uploadImagen(
        @Header("Authorization") token: String,
        @Part imagen: okhttp3.MultipartBody.Part
    ): Response<UploadImagenResponseDTO>

    @DELETE("/api/imagenes/{filename}")
    suspend fun eliminarImagen(
        @Header("Authorization") token: String,
        @Path("filename") filename: String
    ): Response<Unit>

    @POST("/api/envios/crear")
    suspend fun crearEnvio(
        @Header("Authorization") token: String,
        @Body body: CrearEnvioDTO
    ): Response<EnvioDTO>

    @GET("/api/envios/pedido/{pedidoId}")
    suspend fun getEnvioPorPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): Response<EnvioDTO>

    @GET("/api/envios/seguimiento/{numeroSeguimiento}")
    suspend fun getEnvioPorSeguimiento(
        @Header("Authorization") token: String,
        @Path("numeroSeguimiento") numeroSeguimiento: String
    ): Response<EnvioDTO>

    @GET("/api/envios/{envioId}")
    suspend fun getEnvioPorId(
        @Header("Authorization") token: String,
        @Path("envioId") envioId: Int
    ): Response<EnvioDTO>

    @PUT("/api/envios/admin/{envioId}/estado")
    suspend fun actualizarEstadoEnvioAdmin(
        @Header("Authorization") token: String,
        @Path("envioId") envioId: Int,
        @Body body: ActualizarEstadoEnvioDTO
    ): Response<EnvioDTO>

    @PUT("/api/envios/admin/{envioId}/empresa")
    suspend fun asignarEmpresaEnvioAdmin(
        @Header("Authorization") token: String,
        @Path("envioId") envioId: Int,
        @Body body: AsignarEmpresaDTO
    ): Response<EnvioDTO>

    @PUT("/api/envios/admin/{envioId}/fecha-estimada")
    suspend fun actualizarFechaEstimadaEnvioAdmin(
        @Header("Authorization") token: String,
        @Path("envioId") envioId: Int,
        @Body body: ActualizarFechaEnvioDTO
    ): Response<EnvioDTO>

    @GET("/api/envios/admin/estado/{estado}")
    suspend fun getEnviosPorEstadoAdmin(
        @Header("Authorization") token: String,
        @Path("estado") estado: String
    ): Response<List<EnvioDTO>>

    @GET("/api/inventario")
    suspend fun getInventarioAdmin(
        @Header("Authorization") token: String
    ): Response<List<InventarioItemDTO>>

    @GET("/api/inventario/producto/{productoId}")
    suspend fun getInventarioPorProducto(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int
    ): Response<InventarioItemDTO>

    @GET("/api/inventario/bajo-stock")
    suspend fun getBajoStock(
        @Header("Authorization") token: String
    ): Response<List<InventarioItemDTO>>

    @POST("/api/inventario/crear")
    suspend fun crearInventario(
        @Header("Authorization") token: String,
        @Body body: CrearInventarioDTO
    ): Response<InventarioItemDTO>

    @PUT("/api/inventario/producto/{productoId}/actualizar-stock")
    suspend fun actualizarStock(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int,
        @Body body: ActualizarStockDTO
    ): Response<InventarioItemDTO>

    @PUT("/api/inventario/producto/{productoId}/agregar-stock")
    suspend fun agregarStock(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int,
        @Body body: AgregarStockDTO
    ): Response<InventarioItemDTO>

    @PUT("/api/inventario/producto/{productoId}/reducir-stock")
    suspend fun reducirStock(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int,
        @Body body: ReducirStockDTO
    ): Response<InventarioItemDTO>

    @PUT("/api/inventario/producto/{productoId}/stock-minimo")
    suspend fun actualizarStockMinimo(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int,
        @Body body: StockMinimoDTO
    ): Response<InventarioItemDTO>

    @GET("/api/inventario/producto/{productoId}/disponibilidad")
    suspend fun verificarDisponibilidad(
        @Header("Authorization") token: String,
        @Path("productoId") productoId: Int,
        @Query("cantidad") cantidad: Int
    ): Response<DisponibilidadResponseDTO>

    @GET("/api/admin/estadisticas/resumen")
    suspend fun getDashboardResumen(
        @Header("Authorization") token: String
    ): Response<DashboardResumenDTO>

    @GET("/api/admin/estadisticas/top-productos")
    suspend fun getTopProductos(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int
    ): Response<List<TopProductoDTO>>

    @GET("/api/admin/estadisticas/ingresos")
    suspend fun getIngresosDiarios(
        @Header("Authorization") token: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Response<List<SerieIngresoDTO>>

    @GET("/api/admin/estadisticas/ingresos-categoria")
    suspend fun getIngresosPorCategoria(
        @Header("Authorization") token: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Response<List<IngresoCategoriaDTO>>

    @GET("/api/admin/estadisticas/aov")
    suspend fun getAOV(
        @Header("Authorization") token: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Response<AOVDTO>

    @GET("/api/admin/estadisticas/pagos-metodos")
    suspend fun getPagosPorMetodo(
        @Header("Authorization") token: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Response<List<PagosMetodoDTO>>
}