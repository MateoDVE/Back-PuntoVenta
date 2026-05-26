package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa ProductoRepositoryPort usando Supabase.
 */
@Component
public class SupabaseProductoRepositoryAdapter implements ProductoRepositoryPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE = "productos";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseProductoRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public List<Producto> obtenerTodos() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("order", "id_producto.asc");
        queryParams.put("select", "*");
        queryParams.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return rows.stream()
                .map(this::mapToProducto)
                .toList();
    }

    @Override
    public Optional<Producto> obtenerPorId(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_producto", "eq." + id);
        queryParams.put("select", "*");
        queryParams.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToProducto(rows.get(0)));
    }

    @Override
    public Optional<Producto> obtenerPorSku(String sku) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sku", "eq." + sku);
        queryParams.put("select", "*");
        queryParams.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToProducto(rows.get(0)));
    }

    @Override
    public Producto crear(Producto producto) {
        Map<String, Object> body = new HashMap<>();
        body.put("sku", producto.getSku());
        body.put("id_categoria", producto.getIdCategoria());
        body.put("nombre", producto.getNombre());
        body.put("precio_unidad", producto.getPrecioUnidad());
        body.put("precio_caja", producto.getPrecioCaja());
        body.put("unidades_por_caja", producto.getUnidadesPorCaja());
        body.put("stock_almacen_central", producto.getStockAlmacenCentral());
        body.put("descripcion", producto.getDescripcion());
        body.put("url_imagen", producto.getUrlImagen());

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, body, null, "return=representation");
        if (result.isEmpty()) {
            throw new RuntimeException("Error al crear producto");
        }

        return mapToProducto(result.get(0));
    }

    @Override
    public Producto actualizar(String id, Producto producto) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_producto", "eq." + id);

        Map<String, Object> body = new HashMap<>();
        body.put("sku", producto.getSku());
        body.put("id_categoria", producto.getIdCategoria());
        body.put("nombre", producto.getNombre());
        body.put("precio_unidad", producto.getPrecioUnidad());
        body.put("precio_caja", producto.getPrecioCaja());
        body.put("unidades_por_caja", producto.getUnidadesPorCaja());
        body.put("stock_almacen_central", producto.getStockAlmacenCentral());
        body.put("descripcion", producto.getDescripcion());
        body.put("url_imagen", producto.getUrlImagen());

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        return mapToProducto(result.get(0));
    }

    @Override
    public void eliminar(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_producto", "eq." + id);
        
        Map<String, Object> body = new HashMap<>();
        body.put("is_delete", true);
        
        supabaseHttpClient.update(TABLE, queryParams, body);
    }

    @Override
    public boolean existeSku(String sku, String excludeId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sku", "ilike." + sku);
        if (excludeId != null) {
            queryParams.put("id_producto", "neq." + excludeId);
        }
        queryParams.put("select", "id_producto");
        queryParams.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return !rows.isEmpty();
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

        return new Producto(
                id,
                sku,
                idCategoria,
                nombre,
                precioUnidad,
                precioCaja,
                unidadesPorCaja,
                stockAlmacenCentral,
                descripcion,
                urlImagen,
                estado,
                createdAt
        );
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
}
