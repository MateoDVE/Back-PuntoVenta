package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.ventas.model.CargaTransporte;
import com.back.puntoventa.app.domain.ventas.model.DetalleVenta;
import com.back.puntoventa.app.domain.ventas.model.Venta;
import com.back.puntoventa.app.domain.ventas.port.VentaRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Adaptador secundario: Implementa VentaRepositoryPort usando Supabase.
 */
@Component
public class SupabaseVentaRepositoryAdapter implements VentaRepositoryPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE_VENTAS = "ventas";
    private static final String TABLE_DETALLE_VENTA = "detalle_venta";
    private static final String TABLE_PRODUCTOS = "productos";
    private static final String TABLE_CLIENTES = "clientes";
    private static final String TABLE_USUARIOS = "usuarios";
    private static final String TABLE_CARGA_TRANSPORTE = "carga_transporte";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseVentaRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public boolean clienteExiste(Integer idCliente) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_cliente", "eq." + idCliente);
        queryParams.put("select", "id_cliente");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_CLIENTES, queryParams);
        return !rows.isEmpty();
    }

    @Override
    public boolean vendedorExiste(UUID idVendedor) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_usuario", "eq." + idVendedor);
        queryParams.put("estado", "eq.ACTIVO");
        queryParams.put("select", "id_usuario");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_USUARIOS, queryParams);
        return !rows.isEmpty();
    }

    @Override
    public List<Producto> obtenerProductosPorIds(List<String> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String idsStr = String.join(",", ids);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_producto", "in.(" + idsStr + ")");
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_PRODUCTOS, queryParams);
        return rows.stream()
                .map(this::mapToProducto)
                .toList();
    }

    @Override
    public Venta crearVenta(Venta venta) {
        Map<String, Object> body = new HashMap<>();
        body.put("id_cliente", venta.getIdCliente());
        body.put("id_vendedor", venta.getIdVendedor().toString());
        body.put("fecha_hora", venta.getFechaHora().format(ISO_FORMATTER));
        body.put("subtotal", venta.getSubtotal());
        body.put("descuento", venta.getDescuento());
        body.put("total_efectivo", venta.getTotalEfectivo());
        body.put("estado", venta.getEstado());
        body.put("id_transaccion_local", venta.getIdTransaccionLocal());

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE_VENTAS, body, null, "return=representation");
        if (result.isEmpty()) {
            throw new RuntimeException("Error al crear venta");
        }

        return mapToVenta(result.get(0));
    }

    @Override
    public List<DetalleVenta> crearDetallesVenta(List<DetalleVenta> detalles) {
        List<Map<String, Object>> bodies = detalles.stream()
                .map(this::mapDetalleToBody)
                .toList();

        // Supabase permite insert múltiple
        List<Map<String, Object>> results = supabaseHttpClient.insert(TABLE_DETALLE_VENTA, bodies, null, "return=representation");
        if (results.size() != detalles.size()) {
            throw new RuntimeException("Error al crear detalles de venta");
        }

        return results.stream()
                .map(this::mapToDetalleVenta)
                .toList();
    }

    @Override
    public void actualizarStock(String idProducto, Integer nuevaCantidad) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_producto", "eq." + idProducto);

        Map<String, Object> body = new HashMap<>();
        body.put("stock_almacen_central", nuevaCantidad);

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE_PRODUCTOS, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Producto no encontrado para actualizar stock: " + idProducto);
        }
    }

    @Override
    public void actualizarCargaStock(String idCarga, Integer nuevaCantidad) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_carga", "eq." + idCarga);

        Map<String, Object> body = new HashMap<>();
        body.put("cantidad_actual", nuevaCantidad);

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE_CARGA_TRANSPORTE, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Carga de transporte no encontrada para actualizar stock: " + idCarga);
        }
    }

    @Override
    public List<Venta> obtenerVentas() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("select", "*");
        queryParams.put("order", "fecha_hora.desc");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_VENTAS, queryParams);
        return rows.stream()
                .map(this::mapToVenta)
                .toList();
    }

    @Override
    public Venta obtenerVentaPorId(String idVenta) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_venta", "eq." + idVenta);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_VENTAS, queryParams);
        if (rows.isEmpty()) {
            return null;
        }
        return mapToVenta(rows.get(0));
    }

    @Override
    public List<DetalleVenta> obtenerDetallesPorVentaId(String idVenta) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_venta", "eq." + idVenta);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_DETALLE_VENTA, queryParams);
        return rows.stream()
                .map(this::mapToDetalleVenta)
                .toList();
    }

    @Override
    public List<Venta> obtenerVentasPorVendedorYFecha(UUID idVendedor, java.time.LocalDate fecha) {
        List<Map.Entry<String, String>> queryParams = List.of(
                Map.entry("id_vendedor", "eq." + idVendedor),
                Map.entry("fecha_hora", "gte." + fecha.atStartOfDay().format(ISO_FORMATTER)),
                Map.entry("fecha_hora", "lt." + fecha.plusDays(1).atStartOfDay().format(ISO_FORMATTER)),
                Map.entry("select", "*"));

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_VENTAS, queryParams);
        return rows.stream()
                .map(this::mapToVenta)
                .toList();
    }

    @Override
    public List<DetalleVenta> obtenerDetallesPorVentasIds(List<String> idsVentas) {
        if (idsVentas.isEmpty()) {
            return List.of();
        }
        String idsIn = String.join(",", idsVentas);
        List<Map.Entry<String, String>> queryParams = List.of(
                Map.entry("id_venta", "in.(" + idsIn + ")"),
                Map.entry("select", "*"));

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_DETALLE_VENTA, queryParams);
        return rows.stream()
                .map(this::mapToDetalleVenta)
                .toList();
    }

    @Override
    public List<CargaTransporte> obtenerCargasPorVendedorYFecha(UUID idVendedor, java.time.LocalDate fecha) {
        List<Map.Entry<String, String>> queryParams = List.of(
                Map.entry("id_vendedor", "eq." + idVendedor),
                Map.entry("fecha_asignacion", "eq." + fecha),
                Map.entry("select", "*"));

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_CARGA_TRANSPORTE, queryParams);
        return rows.stream()
                .map(this::mapToCargaTransporte)
                .toList();
    }
    @Override
    public Optional<CargaTransporte> obtenerCargaPorVendedorYProducto(UUID idVendedor, String idProducto, java.time.LocalDate fecha) {
        List<Map.Entry<String, String>> queryParams = List.of(
                Map.entry("id_vendedor", "eq." + idVendedor),
                Map.entry("id_producto", "eq." + idProducto),
                Map.entry("estado_validacion", "eq.VALIDADO"),
                Map.entry("fecha_asignacion", "eq." + fecha.toString()),
                Map.entry("order", "fecha_asignacion.desc"),
                Map.entry("select", "*"));

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_CARGA_TRANSPORTE, queryParams);
        return rows.stream().map(this::mapToCargaTransporte).findFirst();
    }

    @Override
    public void actualizarCantidadActualCarga(String idCarga, Integer nuevaCantidad) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_carga", "eq." + idCarga);

        Map<String, Object> body = new HashMap<>();
        body.put("cantidad_actual", nuevaCantidad);

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE_CARGA_TRANSPORTE, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Carga no encontrada para actualizar cantidad: " + idCarga);
        }
    }

    @Override
    public Optional<Venta> findByIdTransaccionLocal(String idTransaccionLocal) {
    if (idTransaccionLocal == null || idTransaccionLocal.isBlank()) {
        return Optional.empty();
    }

    // Usamos la misma lógica que tienes arriba en la línea 200
    Map<String, String> queryParams = Map.of("id_transaccion_local", "eq." + idTransaccionLocal);
    
    // Suponiendo que tu tabla se llama "ventas"
    List<Map<String, Object>> rows = supabaseHttpClient.select("ventas", queryParams);
    
    return rows.stream()
            .map(this::mapToVenta) // Usa tu método que convierte el Map de Supabase a objeto Venta
            .findFirst();
}

    private Map<String, Object> mapDetalleToBody(DetalleVenta detalle) {
        Map<String, Object> body = new HashMap<>();
        body.put("id_venta", detalle.getIdVenta());
        body.put("id_producto", detalle.getIdProducto());
        body.put("cantidad", detalle.getCantidad());
        body.put("tipo_unidad", detalle.getTipoUnidad());
        body.put("precio_unitario", detalle.getPrecioUnitario());
        body.put("subtotal", detalle.getSubtotal());
        return body;
    }

    private Venta mapToVenta(Map<String, Object> row) {
        String idVenta = getString(row.get("id_venta"));
        Integer idCliente = getInteger(row.get("id_cliente"));
        UUID idVendedor = UUID.fromString(getString(row.get("id_vendedor")));
        LocalDateTime fechaHora = null;
        Object fechaHoraObj = row.get("fecha_hora");
        if (fechaHoraObj instanceof String fechaHoraStr) {
            try {
                fechaHora = LocalDateTime.parse(fechaHoraStr, ISO_FORMATTER);
            } catch (Exception e) {
                // Ignorar formato inválido
            }
        }
        BigDecimal subtotal = getBigDecimal(row.get("subtotal"));
        BigDecimal descuento = getBigDecimal(row.get("descuento"));
        BigDecimal totalEfectivo = getBigDecimal(row.get("total_efectivo"));
        String estado = getString(row.get("estado"));
        String idTransaccionLocal = getString(row.get("id_transaccion_local"));
        return new Venta(idVenta, idCliente, idVendedor, fechaHora, subtotal, descuento, totalEfectivo, estado, idTransaccionLocal);
    }

    private DetalleVenta mapToDetalleVenta(Map<String, Object> row) {
        String idDetalle = getString(row.get("id_detalle"));
        String idVenta = getString(row.get("id_venta"));
        String idProducto = getString(row.get("id_producto"));
        Integer cantidad = getInteger(row.get("cantidad"));
        String tipoUnidad = getString(row.get("tipo_unidad"));
        BigDecimal precioUnitario = getBigDecimal(row.get("precio_unitario"));
        BigDecimal subtotal = getBigDecimal(row.get("subtotal"));

        return new DetalleVenta(idDetalle, idVenta, idProducto, cantidad, tipoUnidad, precioUnitario, subtotal);
    }

    private Producto mapToProducto(Map<String, Object> row) {
        String id = getString(row.get("id_producto"));
        String sku = getString(row.get("sku"));
        Integer idCategoria = getInteger(row.get("id_categoria"));
        String nombre = getString(row.get("nombre"));
        Double precioUnidad = getDouble(row.get("precio_unidad"));
        Double precioCaja = getDouble(row.get("precio_caja"));
        Integer unidadesPorCaja = getInteger(row.get("unidades_por_caja"));
        Integer stockAlmacenCentral = getInteger(row.get("stock_almacen_central"));
        String descripcion = getString(row.get("descripcion"));
        String urlImagen = getString(row.get("url_imagen"));
        String estado = getString(row.get("estado"));

        LocalDateTime createdAt = null;
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof String createdAtStr) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr, ISO_FORMATTER);
            } catch (Exception e) {
                // Ignorar formato inválido
            }
        }

        return new Producto(id, sku, idCategoria, nombre, precioUnidad, precioCaja, unidadesPorCaja,
                stockAlmacenCentral, descripcion, urlImagen, estado, createdAt);
    }

    private CargaTransporte mapToCargaTransporte(Map<String, Object> row) {
        String idCarga = getString(row.get("id_carga"));
        UUID idVendedor = UUID.fromString(getString(row.get("id_vendedor")));
        String idVehiculo = getString(row.get("id_vehiculo"));
        String idProducto = getString(row.get("id_producto"));
        java.time.LocalDate fechaAsignacion = null;
        Object fechaObj = row.get("fecha_asignacion");
        if (fechaObj instanceof String fechaStr) {
            try {
                fechaAsignacion = java.time.LocalDate.parse(fechaStr);
            } catch (Exception e) {
                // Ignorar formato inválido
            }
        }
        Integer cantidadInicial = getInteger(row.get("cantidad_inicial"));
        Integer cantidadActual = getInteger(row.get("cantidad_actual"));
        String estadoValidacion = getString(row.get("estado_validacion"));

        return new CargaTransporte(idCarga, idVendedor, idVehiculo, idProducto, fechaAsignacion,
                cantidadInicial, cantidadActual, estadoValidacion);
    }

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Integer getInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Long l) {
            return l.intValue();
        }
        if (value instanceof Double d) {
            return d.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Double getDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double d) {
            return d;
        }
        if (value instanceof Integer i) {
            return i.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Double d) {
            return BigDecimal.valueOf(d);
        }
        if (value instanceof Integer i) {
            return BigDecimal.valueOf(i);
        }
        if (value instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}