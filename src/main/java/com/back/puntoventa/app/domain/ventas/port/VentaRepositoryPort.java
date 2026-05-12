package com.back.puntoventa.app.domain.ventas.port;

import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.ventas.model.CargaTransporte;
import com.back.puntoventa.app.domain.ventas.model.DetalleVenta;
import com.back.puntoventa.app.domain.ventas.model.Venta;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

/**
 * Puerto: Contrato para persistencia de ventas.
 */
public interface VentaRepositoryPort {
    boolean clienteExiste(Integer idCliente);
    boolean vendedorExiste(UUID idVendedor);
    List<Producto> obtenerProductosPorIds(List<String> ids);
    Venta crearVenta(Venta venta);
    List<DetalleVenta> crearDetallesVenta(List<DetalleVenta> detalles);
    void actualizarStock(String idProducto, Integer nuevaCantidad);
    List<Venta> obtenerVentas();
    Venta obtenerVentaPorId(String idVenta);
    List<DetalleVenta> obtenerDetallesPorVentaId(String idVenta);
    List<Venta> obtenerVentasPorVendedorYFecha(UUID idVendedor, java.time.LocalDate fecha);
    List<DetalleVenta> obtenerDetallesPorVentasIds(List<String> idsVentas);
    List<CargaTransporte> obtenerCargasPorVendedorYFecha(UUID idVendedor, java.time.LocalDate fecha);
    Optional<CargaTransporte> obtenerCargaPorVendedorYProducto(UUID idVendedor, String idProducto);
    void actualizarCantidadActualCarga(String idCarga, Integer nuevaCantidad);
    Optional<Venta> findByIdTransaccionLocal(String idTransaccionLocal);
}