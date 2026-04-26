package com.back.puntoventa.app.domain.ventas.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.ventas.model.DetalleVenta;
import com.back.puntoventa.app.domain.ventas.model.Venta;
import com.back.puntoventa.app.domain.ventas.model.response.CierreJornadaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ConciliacionInventarioResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleProductoConciliacionResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleVentaCierreResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ResumenFinancieroResponse;
import com.back.puntoventa.app.domain.ventas.model.request.CrearVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.request.ItemVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleVentaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ResumenDiarioResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResumenResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResponse;
import com.back.puntoventa.app.domain.ventas.port.VentaRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    public List<VentaResumenResponse> obtenerVentas() {
        logger.info("Obteniendo listado de ventas resumidas");
        return ventaRepositoryPort.obtenerVentas().stream()
                .map(venta -> new VentaResumenResponse(
                        venta.getIdVenta(),
                        venta.getIdCliente(),
                        venta.getIdVendedor(),
                        venta.getFechaHora(),
                        venta.getSubtotal(),
                        venta.getDescuento(),
                        venta.getTotalEfectivo(),
                        venta.getEstado()))
                .toList();
    }

    public VentaResponse obtenerVentaPorId(String idVenta) {
        logger.info("Obteniendo venta por id: {}", idVenta);
        if (idVenta == null || idVenta.isBlank()) {
            throw new IllegalArgumentException("ID de venta es obligatorio");
        }

        Venta venta = ventaRepositoryPort.obtenerVentaPorId(idVenta);
        if (venta == null) {
            logger.warn("Venta no encontrada: {}", idVenta);
            throw new DomainException("Venta no encontrada");
        }

        List<DetalleVenta> detalles = ventaRepositoryPort.obtenerDetallesPorVentaId(idVenta);
        List<DetalleVentaResponse> detallesResponse = detalles.stream()
                .map(d -> new DetalleVentaResponse(d.getIdDetalle(), d.getIdProducto(), d.getCantidad(),
                        d.getTipoUnidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();

        return new VentaResponse(
                venta.getIdVenta(),
                venta.getIdCliente(),
                venta.getIdVendedor(),
                venta.getFechaHora(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotalEfectivo(),
                venta.getEstado(),
                detallesResponse);
    }

    public ResumenDiarioResponse obtenerResumenDiario(UUID idVendedor, LocalDate fecha) {
        logger.info("Obteniendo resumen diario para vendedor {} en fecha {}", idVendedor, fecha);
        if (idVendedor == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }

        validarVendedor(idVendedor);
        List<Venta> ventas = ventaRepositoryPort.obtenerVentasPorVendedorYFecha(idVendedor, fecha);

        int cantidadVentas = ventas.size();
        BigDecimal montoTotalVendido = ventas.stream()
                .map(Venta::getTotalEfectivo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDescuentos = ventas.stream()
                .map(Venta::getDescuento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalProductosVendidos = ventas.stream()
                .flatMap(venta -> ventaRepositoryPort.obtenerDetallesPorVentaId(venta.getIdVenta()).stream())
                .mapToInt(DetalleVenta::getCantidad)
                .sum();

        BigDecimal dineroEsperado = montoTotalVendido;
        return new ResumenDiarioResponse(idVendedor, fecha, cantidadVentas, montoTotalVendido, totalProductosVendidos,
                totalDescuentos, dineroEsperado);
    }

    public CierreJornadaResponse obtenerResumenCierreJornada(UUID idVendedor, LocalDate fecha) {
        logger.info("Obteniendo resumen de cierre de jornada para vendedor {} en fecha {}", idVendedor, fecha);
        if (idVendedor == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }

        validarVendedor(idVendedor);

        // Obtener ventas del día
        List<Venta> ventas = ventaRepositoryPort.obtenerVentasPorVendedorYFecha(idVendedor, fecha);
        List<String> idsVentas = ventas.stream().map(Venta::getIdVenta).toList();

        // Obtener detalles de ventas
        List<DetalleVenta> detallesVentas = ventaRepositoryPort.obtenerDetallesPorVentasIds(idsVentas);

        // Obtener productos vendidos
        List<String> idsProductosVendidos = detallesVentas.stream()
                .map(DetalleVenta::getIdProducto)
                .distinct()
                .toList();

        List<Producto> productos = ventaRepositoryPort.obtenerProductosPorIds(idsProductosVendidos);
        Map<String, Producto> productosMap = productos.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        // Calcular resumen financiero
        Integer ventasRealizadas = ventas.size();
        BigDecimal totalEfectivo = ventas.stream()
                .map(Venta::getTotalEfectivo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDescuentos = ventas.stream()
                .map(Venta::getDescuento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<DetalleVentaCierreResponse> detalleVentasResponse = ventas.stream()
                .map(v -> new DetalleVentaCierreResponse(v.getIdVenta(), v.getFechaHora(), v.getSubtotal(),
                        v.getDescuento(), v.getTotalEfectivo(), v.getEstado()))
                .toList();
        ResumenFinancieroResponse resumenFinanciero = new ResumenFinancieroResponse(ventasRealizadas,
                totalEfectivo, totalDescuentos, detalleVentasResponse);

        // Calcular conciliación de inventario
        Map<String, Integer> vendidosPorProducto = detallesVentas.stream()
                .collect(Collectors.groupingBy(DetalleVenta::getIdProducto,
                        Collectors.summingInt(DetalleVenta::getCantidad)));

        Integer stockInicialTotal = 0;
        Integer vendidosTotal = detallesVentas.stream().mapToInt(DetalleVenta::getCantidad).sum();
        Integer stockFinalTotal = 0;
        List<DetalleProductoConciliacionResponse> detalleProductos = new ArrayList<>();
        boolean conciliacionCorrecta = true;

        for (String idProducto : idsProductosVendidos) {
            Producto producto = productosMap.get(idProducto);
            if (producto == null) continue;

            Integer vendido = vendidosPorProducto.getOrDefault(idProducto, 0);
            Integer actual = producto.getStockAlmacenCentral() != null ? producto.getStockAlmacenCentral() : 0;
            Integer stockInicial = actual + vendido;
            Integer esperado = stockInicial - vendido;

            if (!esperado.equals(actual)) {
                conciliacionCorrecta = false;
            }

            stockInicialTotal += stockInicial;
            stockFinalTotal += actual;

            detalleProductos.add(new DetalleProductoConciliacionResponse(idProducto, producto.getNombre(),
                    stockInicial, vendido, esperado, actual));
        }

        String estadoConciliacion = conciliacionCorrecta ? "CORRECTO" : "DIFERENCIA";
        ConciliacionInventarioResponse conciliacionInventario = new ConciliacionInventarioResponse(
                stockInicialTotal, vendidosTotal, stockFinalTotal, estadoConciliacion, detalleProductos);

        return new CierreJornadaResponse(idVendedor, fecha, resumenFinanciero, conciliacionInventario);
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