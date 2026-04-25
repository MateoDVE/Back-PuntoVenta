package com.back.puntoventa.app.domain.ventas.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.ventas.model.DetalleVenta;
import com.back.puntoventa.app.domain.ventas.model.Venta;
import com.back.puntoventa.app.domain.ventas.model.request.CrearVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.request.ItemVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleVentaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResponse;
import com.back.puntoventa.app.domain.ventas.port.VentaRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: Gestión de ventas.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionVentasService {

    private static final Logger logger = LoggerFactory.getLogger(GestionVentasService.class);
    private final VentaRepositoryPort ventaRepositoryPort;

    public GestionVentasService(VentaRepositoryPort ventaRepositoryPort) {
        this.ventaRepositoryPort = ventaRepositoryPort;
    }

    public VentaResponse crearVenta(CrearVentaRequest request) {
        logger.info("Creando venta para cliente {} por vendedor {}", request.getIdCliente(), request.getIdVendedor());

        // Validaciones iniciales
        validarCliente(request.getIdCliente());
        validarVendedor(request.getIdVendedor());

        // Obtener productos
        List<String> idsProductos = request.getItems().stream()
                .map(ItemVentaRequest::getIdProducto)
                .toList();
        List<Producto> productos = ventaRepositoryPort.obtenerProductosPorIds(idsProductos);

        Map<String, Producto> productosMap = productos.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        // Validar productos y stock
        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleLinea> lineas = new ArrayList<>();

        for (ItemVentaRequest item : request.getItems()) {
            Producto producto = productosMap.get(item.getIdProducto());
            if (producto == null) {
                logger.warn("Producto no encontrado: {}", item.getIdProducto());
                throw new DomainException("Producto no encontrado: " + item.getIdProducto());
            }

            Integer stockActual = producto.getStockAlmacenCentral() != null ? producto.getStockAlmacenCentral() : 0;
            if (stockActual < item.getCantidad()) {
                logger.warn("Stock insuficiente para producto {}: disponible {}, solicitado {}",
                        item.getIdProducto(), stockActual, item.getCantidad());
                throw new DomainException("Stock insuficiente para producto " + producto.getNombre());
            }

            BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecioUnidad() != null ? producto.getPrecioUnidad() : 0.0);
            BigDecimal subtotalItem = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(subtotalItem);

            lineas.add(new DetalleLinea(item.getIdProducto(), item.getCantidad(), item.getTipoUnidad(), precioUnitario, subtotalItem));
        }

        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal totalEfectivo = subtotal.subtract(descuento);

        // Crear venta
        Venta venta = new Venta(
                null, // idVenta se genera en BD
                request.getIdCliente(),
                request.getIdVendedor(),
                LocalDateTime.now(),
                subtotal,
                descuento,
                totalEfectivo,
                "COMPLETADA"
        );

        Venta ventaCreada = ventaRepositoryPort.crearVenta(venta);
        logger.info("Venta creada con ID: {}", ventaCreada.getIdVenta());

        // Construir detalles con idVenta ya disponible
        List<DetalleVenta> detalles = lineas.stream()
                .map(linea -> new DetalleVenta(
                        null,
                        ventaCreada.getIdVenta(),
                        linea.idProducto(),
                        linea.cantidad(),
                        linea.tipoUnidad(),
                        linea.precioUnitario(),
                        linea.subtotal()))
                .toList();

        // Crear detalles
        detalles = ventaRepositoryPort.crearDetallesVenta(detalles);
        logger.debug("Detalles de venta creados: {}", detalles.size());

        // Actualizar stock
        for (DetalleVenta detalle : detalles) {
            Producto producto = productosMap.get(detalle.getIdProducto());
            Integer nuevoStock = (producto.getStockAlmacenCentral() != null ? producto.getStockAlmacenCentral() : 0) - detalle.getCantidad();
            ventaRepositoryPort.actualizarStock(detalle.getIdProducto(), nuevoStock);
            logger.debug("Stock actualizado para producto {}: {}", detalle.getIdProducto(), nuevoStock);
        }

        // Construir response
        List<DetalleVentaResponse> detallesResponse = detalles.stream()
                .map(d -> new DetalleVentaResponse(d.getIdDetalle(), d.getIdProducto(), d.getCantidad(),
                        d.getTipoUnidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();

        VentaResponse response = new VentaResponse(
                ventaCreada.getIdVenta(),
                ventaCreada.getIdCliente(),
                ventaCreada.getIdVendedor(),
                ventaCreada.getFechaHora(),
                ventaCreada.getSubtotal(),
                ventaCreada.getDescuento(),
                ventaCreada.getTotalEfectivo(),
                ventaCreada.getEstado(),
                detallesResponse
        );

        logger.info("Venta completada exitosamente - ID: {}, Total: {}", ventaCreada.getIdVenta(), totalEfectivo);
        return response;
    }

    private void validarCliente(Integer idCliente) {
        if (!ventaRepositoryPort.clienteExiste(idCliente)) {
            logger.warn("Cliente no encontrado: {}", idCliente);
            throw new DomainException("Cliente no encontrado");
        }
    }

    private void validarVendedor(UUID idVendedor) {
        if (!ventaRepositoryPort.vendedorExiste(idVendedor)) {
            logger.warn("Vendedor no encontrado: {}", idVendedor);
            throw new DomainException("Vendedor no encontrado");
        }
    }

    private static record DetalleLinea(String idProducto, Integer cantidad, String tipoUnidad,
            BigDecimal precioUnitario, BigDecimal subtotal) {
    }
}