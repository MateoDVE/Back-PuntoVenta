package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.inventario_ruta.model.CargaTransporte;
import com.back.puntoventa.app.domain.inventario_ruta.port.InventarioRutaRepositoryPort;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa InventarioRutaRepositoryPort usando Supabase.
 */
@Component
public class SupabaseInventarioRutaRepositoryAdapter implements InventarioRutaRepositoryPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE_CARGA_TRANSPORTE = "carga_transporte";
    private static final String TABLE_PRODUCTOS = "productos";
    private static final String TABLE_USUARIOS = "usuarios";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseInventarioRutaRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
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
    public List<CargaTransporte> obtenerCargasPorVendedorYFecha(UUID idVendedor, LocalDate fecha) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_vendedor", "eq." + idVendedor);
        queryParams.put("fecha_asignacion", "eq." + fecha);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_CARGA_TRANSPORTE, queryParams);
        return rows.stream()
                .map(this::mapToCargaTransporte)
                .toList();
    }

    @Override
    public List<Producto> obtenerProductosPorIds(List<String> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String idsStr = String.join(",", ids);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "in.(" + idsStr + ")");
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_PRODUCTOS, queryParams);
        return rows.stream()
                .map(this::mapToProducto)
                .toList();
    }

    @Override
    public void actualizarCantidadActual(Integer idCarga, Integer cantidadActual) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_carga", "eq." + idCarga);

        Map<String, Object> body = new HashMap<>();
        body.put("cantidad_actual", cantidadActual);

        supabaseHttpClient.update(TABLE_CARGA_TRANSPORTE, queryParams, body);
    }

    @Override
    public void validarCargas(List<Integer> idsCargas) {
        if (idsCargas.isEmpty()) {
            return;
        }
        String idsStr = idsCargas.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_carga", "in.(" + idsStr + ")");

        Map<String, Object> body = new HashMap<>();
        body.put("estado_validacion", "VALIDADO");

        supabaseHttpClient.update(TABLE_CARGA_TRANSPORTE, queryParams, body);
    }

    private CargaTransporte mapToCargaTransporte(Map<String, Object> row) {
        Integer idCarga = getInteger(row.get("id_carga"));
        UUID idVendedor = UUID.fromString(getString(row.get("id_vendedor")));
        Integer idVehiculo = getInteger(row.get("id_vehiculo"));
        String idProducto = getString(row.get("id_producto"));
        LocalDate fechaAsignacion = null;
        Object fechaObj = row.get("fecha_asignacion");
        if (fechaObj instanceof String fechaStr) {
            try {
                fechaAsignacion = LocalDate.parse(fechaStr);
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

    private Producto mapToProducto(Map<String, Object> row) {
        String id = getString(row.get("id"));
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

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Integer getInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
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
            return null;
        }
        if (value instanceof Double d) {
            return d;
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}