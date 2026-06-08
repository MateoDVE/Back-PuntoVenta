package com.back.puntoventa.app.domain.ventas.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.ventas.model.CargaTransporte;
import com.back.puntoventa.app.domain.ventas.model.DetalleVenta;
import com.back.puntoventa.app.domain.ventas.model.Venta;
import com.back.puntoventa.app.domain.ventas.model.response.CierreJornadaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ConciliacionInventarioResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleProductoConciliacionResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleVentaCierreResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ItemVentaCierreResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ResumenFinancieroResponse;
import com.back.puntoventa.app.domain.ventas.model.request.CrearVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.request.ItemVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.response.ConfirmarCierreResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DetalleVentaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ResumenDiarioResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResumenResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResponse;
import com.back.puntoventa.app.domain.ventas.port.VentaRepositoryPort;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import com.back.puntoventa.app.domain.vendedores.port.VendedorRepositoryPort;
import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import com.back.puntoventa.app.domain.ventas.model.response.ReportesResumenResponse;
import com.back.puntoventa.app.domain.ventas.model.response.DiscrepanciaVendedorDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.back.puntoventa.app.domain.cierre.port.CierreJornadaRepositoryPort;

/**
 * Caso de uso: Gestión de ventas.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionVentasService {

    private static final Logger logger = LoggerFactory.getLogger(GestionVentasService.class);
    private final VentaRepositoryPort ventaRepositoryPort;
    private final VendedorRepositoryPort vendedorRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final CierreJornadaRepositoryPort cierreJornadaRepositoryPort;

    public GestionVentasService(VentaRepositoryPort ventaRepositoryPort,
                                VendedorRepositoryPort vendedorRepositoryPort,
                                ProductoRepositoryPort productoRepositoryPort,
                                CierreJornadaRepositoryPort cierreJornadaRepositoryPort) {
        this.ventaRepositoryPort = ventaRepositoryPort;
        this.vendedorRepositoryPort = vendedorRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.cierreJornadaRepositoryPort = cierreJornadaRepositoryPort;
    }

    public ReportesResumenResponse obtenerReportesConsolidados(LocalDate fecha) {
        logger.info("Generando reportes consolidados para la fecha: {}", fecha);
        
        // 1. Obtener todas las ventas resumidas
        List<VentaResumenResponse> ventas = obtenerVentas();
        
        // 2. Obtener todos los vendedores activos
        List<Vendedor> vendedores = vendedorRepositoryPort.obtenerTodos();
        
        // 3. Obtener todos los productos activos
        List<Producto> productos = productoRepositoryPort.obtenerTodos();
        
        // 4. Calcular discrepancias de inventario para cada vendedor
        List<DiscrepanciaVendedorDto> discrepancias = new ArrayList<>();
        for (Vendedor v : vendedores) {
            try {
                CierreJornadaResponse cierre = obtenerResumenCierreJornada(UUID.fromString(v.getId()), fecha);
                var inv = cierre.getConciliacionInventario();
                int esperado = (inv.getStockInicialTotal() != null ? inv.getStockInicialTotal() : 0) 
                             - (inv.getVendidosTotal() != null ? inv.getVendidosTotal() : 0);
                int actual = inv.getStockFinalTotal() != null ? inv.getStockFinalTotal() : 0;
                boolean correcto = "CORRECTO".equals(inv.getEstado());
                
                discrepancias.add(new DiscrepanciaVendedorDto(
                    v.getNombre(),
                    esperado,
                    actual,
                    actual - esperado,
                    correcto
                ));
            } catch (Exception ex) {
                logger.warn("No se pudo calcular el cierre para el vendedor {} en la fecha {}: {}", v.getNombre(), fecha, ex.getMessage());
            }
        }
        
        return new ReportesResumenResponse(ventas, vendedores, productos, discrepancias);
    }

    public VentaResponse crearVenta(CrearVentaRequest request) {
    // --- PASO 1.2.2: ESCUDO DE IDEMPOTENCIA ---
    logger.info("Procesando solicitud de venta. Cliente: {}, Vendedor: {}, ID Local: {}", 
        request.getIdCliente(), request.getIdVendedor(), request.getIdTransaccionLocal());

    // Verificamos si este ID de transacción ya existe para evitar duplicados
    java.util.Optional<Venta> ventaExistente = ventaRepositoryPort.findByIdTransaccionLocal(request.getIdTransaccionLocal());

    if (ventaExistente.isPresent()) {
        Venta v = ventaExistente.get();
        logger.info("Venta duplicada detectada (ID Local: {}). Retornando venta existente ID: {}", 
            request.getIdTransaccionLocal(), v.getIdVenta());
        
        // Recuperamos los detalles para devolver la respuesta completa como si se acabara de crear
        List<DetalleVenta> detalles = ventaRepositoryPort.obtenerDetallesPorVentaId(v.getIdVenta());
        List<DetalleVentaResponse> detallesRes = detalles.stream()
            .map(d -> new DetalleVentaResponse(d.getIdDetalle(), d.getIdProducto(), d.getCantidad(),
                    d.getTipoUnidad(), d.getPrecioUnitario(), d.getSubtotal()))
            .toList();

        return new VentaResponse(
                v.getIdVenta(), v.getIdCliente(), v.getIdVendedor(), v.getFechaHora(),
                v.getSubtotal(), v.getDescuento(), v.getTotalEfectivo(), v.getEstado(), detallesRes);
    }
    // --- FIN ESCUDO ---

    // Validaciones iniciales
    validarCliente(request.getIdCliente());
    validarVendedor(request.getIdVendedor());

    if (cierreJornadaRepositoryPort.existePorVendedorYFecha(request.getIdVendedor(), LocalDate.now())) {
        logger.warn("Intento de venta en jornada ya cerrada para vendedor {}", request.getIdVendedor());
        throw new DomainException("No se pueden realizar ventas porque la jornada de hoy ya está cerrada.");
    }

    // Obtener productos
    List<String> idsProductos = request.getItems().stream()
            .map(ItemVentaRequest::getIdProducto)
            .toList();
    List<Producto> productos = ventaRepositoryPort.obtenerProductosPorIds(idsProductos);

    Map<String, Producto> productosMap = productos.stream()
            .collect(Collectors.toMap(Producto::getId, p -> p));

    // Validar productos y stock contra carga_transporte del vendedor
    BigDecimal subtotal = BigDecimal.ZERO;
    List<DetalleLinea> lineas = new ArrayList<>();
    Map<String, CargaTransporte> cargasMap = new HashMap<>();

    for (ItemVentaRequest item : request.getItems()) {
        Producto producto = productosMap.get(item.getIdProducto());
        if (producto == null) {
            logger.warn("Producto no encontrado: {}", item.getIdProducto());
            throw new DomainException("Producto no encontrado: " + item.getIdProducto());
        }

        CargaTransporte carga = ventaRepositoryPort
                .obtenerCargaPorVendedorYProducto(request.getIdVendedor(), item.getIdProducto(), LocalDate.now())
                .orElseThrow(() -> new DomainException(
                        "No hay carga asignada y validada para producto " + producto.getNombre()));

        Integer stockActual = carga.getCantidadActual() != null ? carga.getCantidadActual() : 0;
        if (stockActual < item.getCantidad()) {
            logger.warn("Stock insuficiente para producto {}: disponible {}, solicitado {}",
                    item.getIdProducto(), stockActual, item.getCantidad());
            throw new DomainException("Stock insuficiente para producto " + producto.getNombre());
        }

        cargasMap.put(item.getIdProducto(), carga);

        BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecioUnidad() != null ? producto.getPrecioUnidad() : 0.0);
        BigDecimal subtotalItem = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));
        subtotal = subtotal.add(subtotalItem);

        lineas.add(new DetalleLinea(item.getIdProducto(), item.getCantidad(), item.getTipoUnidad(), precioUnitario, subtotalItem));
    }

    BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
    BigDecimal totalEfectivo = subtotal.subtract(descuento);

    // Crear venta (IMPORTANTE: Ahora incluimos el idTransaccionLocal)
    Venta venta = new Venta(
            null, // idVenta se genera en BD
            request.getIdCliente(),
            request.getIdVendedor(),
            LocalDateTime.now(),
            subtotal,
            descuento,
            totalEfectivo,
            "COMPLETADA",
            request.getIdTransaccionLocal() // <--- Se añade el ID de la transacción local
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

    // Descontar stock de carga_transporte del vendedor
    for (DetalleVenta detalle : detalles) {
        CargaTransporte carga = cargasMap.get(detalle.getIdProducto());
        Integer nuevaCantidad = (carga.getCantidadActual() != null ? carga.getCantidadActual() : 0) - detalle.getCantidad();
        ventaRepositoryPort.actualizarCantidadActualCarga(carga.getIdCarga(), nuevaCantidad);
        logger.debug("Carga {} actualizada para producto {}: cantidad_actual={}", carga.getIdCarga(), detalle.getIdProducto(), nuevaCantidad);
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

        // Obtener todas las cargas asignadas a ruta el día de hoy
        List<CargaTransporte> cargas = ventaRepositoryPort.obtenerCargasPorVendedorYFecha(idVendedor, fecha);
        java.util.Set<String> allProductIds = new java.util.HashSet<>();
        for (CargaTransporte c : cargas) {
            allProductIds.add(c.getIdProducto());
        }
        allProductIds.addAll(idsProductosVendidos);

        List<Producto> productos = ventaRepositoryPort.obtenerProductosPorIds(new ArrayList<>(allProductIds));
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
        Map<String, List<DetalleVenta>> detallesPorVenta = detallesVentas.stream()
                .collect(Collectors.groupingBy(DetalleVenta::getIdVenta));

        List<DetalleVentaCierreResponse> detalleVentasResponse = ventas.stream()
                .map(v -> {
                    List<ItemVentaCierreResponse> items = detallesPorVenta
                            .getOrDefault(v.getIdVenta(), List.of())
                            .stream()
                            .map(d -> {
                                Producto p = productosMap.get(d.getIdProducto());
                                String nombre = p != null ? p.getNombre() : d.getIdProducto();
                                return new ItemVentaCierreResponse(nombre, d.getCantidad(),
                                        d.getTipoUnidad(), d.getPrecioUnitario(), d.getSubtotal());
                            })
                            .toList();
                    return new DetalleVentaCierreResponse(v.getIdVenta(), v.getFechaHora(),
                            v.getSubtotal(), v.getDescuento(), v.getTotalEfectivo(), v.getEstado(), items);
                })
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

        for (String idProducto : allProductIds) {
            Producto producto = productosMap.get(idProducto);
            if (producto == null) continue;

            Integer vendido = vendidosPorProducto.getOrDefault(idProducto, 0);

            Optional<CargaTransporte> cargaOpt = cargas.stream()
                    .filter(c -> c.getIdProducto().equals(idProducto))
                    .findFirst();
            if (cargaOpt.isEmpty()) {
                cargaOpt = ventaRepositoryPort.obtenerCargaPorVendedorYProducto(idVendedor, idProducto, fecha);
            }
            Integer actual = cargaOpt.map(c -> c.getCantidadActual() != null ? c.getCantidadActual() : 0).orElse(0);
            Integer stockInicial = cargaOpt.map(c -> c.getCantidadInicial() != null ? c.getCantidadInicial() : (actual + vendido)).orElse(actual + vendido);
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

    public ConfirmarCierreResponse confirmarCierreJornada(UUID idVendedor, LocalDate fecha, BigDecimal dineroContado) {
        logger.info("Confirmando cierre de jornada para vendedor {} en fecha {} con dinero contado {}", idVendedor, fecha, dineroContado);
        if (idVendedor == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }
        if (dineroContado == null) {
            throw new IllegalArgumentException("Dinero contado es obligatorio");
        }

        validarVendedor(idVendedor);

        // Obtener ventas del día
        List<Venta> ventas = ventaRepositoryPort.obtenerVentasPorVendedorYFecha(idVendedor, fecha);

        // Calcular dinero esperado: suma de totalEfectivo de las ventas
        BigDecimal dineroEsperado = ventas.stream()
                .map(Venta::getTotalEfectivo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular diferencia
        BigDecimal diferencia = dineroContado.subtract(dineroEsperado);

        // Determinar estado de conciliación
        String estadoConciliacion;
        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            estadoConciliacion = "CORRECTO";
        } else if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            estadoConciliacion = "SOBRANTE";
        } else {
            estadoConciliacion = "FALTANTE";
        }

        logger.info("Conciliación de efectivo completada - Esperado: {}, Contado: {}, Diferencia: {}, Estado: {}",
                dineroEsperado, dineroContado, diferencia, estadoConciliacion);

        return new ConfirmarCierreResponse(dineroEsperado, dineroContado, diferencia, estadoConciliacion);
    }

    public void devolverStockAlmacen(UUID idVendedor, LocalDate fecha) {
        logger.info("Procesando devolución de stock al almacén para vendedor {} y fecha {}", idVendedor, fecha);
        validarVendedor(idVendedor);

        // 1. Obtener todas las cargas del vendedor para esa fecha
        List<CargaTransporte> cargas = ventaRepositoryPort.obtenerCargasPorVendedorYFecha(idVendedor, fecha);

        for (CargaTransporte carga : cargas) {
            Integer cantidadActual = carga.getCantidadActual() != null ? carga.getCantidadActual() : 0;
            if (cantidadActual > 0) {
                // Obtener el producto
                Producto producto = productoRepositoryPort.obtenerPorId(carga.getIdProducto())
                        .orElseThrow(() -> new DomainException("Producto no encontrado: " + carga.getIdProducto()));

                // Calcular nuevo stock central
                Integer stockCentralActual = producto.getStockAlmacenCentral() != null ? producto.getStockAlmacenCentral() : 0;
                Integer nuevoStockCentral = stockCentralActual + cantidadActual;

                // Actualizar stock central del producto
                Producto productoActualizado = new Producto(
                        producto.getId(),
                        producto.getSku(),
                        producto.getIdCategoria(),
                        producto.getNombre(),
                        producto.getPrecioUnidad(),
                        producto.getPrecioCaja(),
                        producto.getUnidadesPorCaja(),
                        nuevoStockCentral,
                        producto.getDescripcion(),
                        producto.getUrlImagen(),
                        producto.getEstado(),
                        producto.getCreatedAt()
                );
                productoRepositoryPort.actualizar(producto.getId(), productoActualizado);
                logger.info("Stock de producto {} devuelto al almacén central. Cantidad devuelta: {}, Nuevo stock central: {}",
                        producto.getNombre(), cantidadActual, nuevoStockCentral);

                // Poner en 0 la cantidad actual de la carga
                ventaRepositoryPort.actualizarCantidadActualCarga(carga.getIdCarga(), 0);
                logger.info("Cantidad actual de carga {} reseteada a 0", carga.getIdCarga());
            }
        }
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