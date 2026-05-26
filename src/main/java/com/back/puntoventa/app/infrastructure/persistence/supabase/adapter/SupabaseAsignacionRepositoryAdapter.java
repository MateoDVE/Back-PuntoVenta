package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.port.AsignacionRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SupabaseAsignacionRepositoryAdapter implements AsignacionRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAsignacionRepositoryAdapter.class);

    private final SupabaseHttpClient supabaseHttpClient;
    private static final String TABLE = "carga_transporte"; // Nombre según tu planeación

    public SupabaseAsignacionRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public AsignacionStock guardar(AsignacionStock asignacion) {
        logger.debug("Guardando asignación en Supabase - vendedor: {}, producto: {}, cantidad: {}",
                asignacion.getIdVendedor(), asignacion.getIdProducto(), asignacion.getCantidadAsignada());
        Map<String, Object> row = new HashMap<>();
        row.put("id_vendedor", asignacion.getIdVendedor());
        row.put("id_producto", parseProductoId(asignacion.getIdProducto()));
        row.put("cantidad_inicial", asignacion.getCantidadAsignada());
        row.put("cantidad_actual", asignacion.getCantidadAsignada());
        row.put("estado_validacion", asignacion.getEstadoValidacion());

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, row, null, "return=representation");
        if (result.isEmpty()) {
            logger.error("Supabase devolvió resultado vacío al guardar asignación");
            throw new IllegalStateException("No se pudo guardar la asignación de stock");
        }

        AsignacionStock guardada = mapToAsignacion(result.get(0));
        logger.info("Asignación guardada correctamente - idCarga: {}", guardada.getIdCarga());
        return guardada;
    }

    @Override
    public List<AsignacionStock> obtenerPorVendedor(String idVendedor) {
        logger.debug("Consultando asignaciones por vendedor: {}", idVendedor);
        Map<String, String> query = new HashMap<>();
        query.put("id_vendedor", "eq." + idVendedor);
        query.put("select", "*");
        query.put("order", "id_carga.desc");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, query);
        logger.debug("Asignaciones encontradas para vendedor {}: {}", idVendedor, rows.size());
        return rows.stream().map(this::mapToAsignacion).toList();
    }

    @Override
    public Optional<AsignacionStock> obtenerPorId(String idCarga) {
        logger.debug("Consultando asignación por idCarga: {}", idCarga);
        Map<String, String> query = new HashMap<>();
        query.put("id_carga", "eq." + idCarga);
        query.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, query);
        if (rows.isEmpty()) {
            logger.warn("No se encontró asignación para idCarga: {}", idCarga);
            return Optional.empty();
        }

        logger.debug("Asignación encontrada para idCarga: {}", idCarga);
        return Optional.of(mapToAsignacion(rows.get(0)));
    }

    @Override
    public AsignacionStock actualizarEstado(String idCarga, String nuevoEstado) {
        logger.debug("Actualizando estado de asignación - idCarga: {}, nuevoEstado: {}", idCarga, nuevoEstado);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_carga", "eq." + idCarga);

        Map<String, Object> body = new HashMap<>();
        body.put("estado_validacion", nuevoEstado);

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, queryParams, body);
        if (result.isEmpty()) {
            logger.warn("No se pudo actualizar estado. idCarga no encontrada: {}", idCarga);
            throw new IllegalArgumentException("No se encontró la carga para actualizar estado");
        }

        AsignacionStock actualizada = mapToAsignacion(result.get(0));
        logger.info("Estado actualizado correctamente - idCarga: {}, estado: {}", idCarga,
                actualizada.getEstadoValidacion());
        return actualizada;
    }

    private AsignacionStock mapToAsignacion(Map<String, Object> row) {
        String idCarga = getString(row.get("id_carga"));
        String idVendedor = getString(row.get("id_vendedor"));
        String idProducto = getString(row.get("id_producto"));
        Integer cantidadInicial = getInteger(row.get("cantidad_inicial"));
        Integer cantidadActual = getInteger(row.get("cantidad_actual"));
        String estadoValidacion = getString(row.get("estado_validacion"));
        LocalDateTime fechaAsignacion = getDateTime(row.get("fecha_asignacion"));

        return new AsignacionStock(
                idCarga,
                idVendedor,
                idProducto,
                cantidadInicial,
                cantidadActual,
                estadoValidacion,
                fechaAsignacion);
    }

    private Integer parseProductoId(String idProducto) {
        try {
            return Integer.parseInt(idProducto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("idProducto debe ser numérico");
        }
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
        if (value instanceof Integer number) {
            return number;
        }
        if (value instanceof Long number) {
            return number.intValue();
        }
        if (value instanceof Double number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime getDateTime(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String text) {
            try {
                return LocalDateTime.parse(text);
            } catch (Exception ignored) {
                try {
                    return LocalDate.parse(text).atStartOfDay();
                } catch (Exception ignored2) {
                    return null;
                }
            }
        }

        return null;
    }

}
