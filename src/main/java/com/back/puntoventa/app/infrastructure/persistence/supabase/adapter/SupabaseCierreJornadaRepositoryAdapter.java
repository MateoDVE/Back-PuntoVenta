package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.cierre.model.CierreJornada;
import com.back.puntoventa.app.domain.cierre.port.CierreJornadaRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador secundario: Implementa CierreJornadaRepositoryPort usando Supabase.
 */
public class SupabaseCierreJornadaRepositoryAdapter implements CierreJornadaRepositoryPort {

    private static final String TABLE = "cierres_jornada";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseCierreJornadaRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public CierreJornada crear(CierreJornada cierre) {
        Map<String, Object> body = buildBody(cierre);
        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, body, null, "return=representation");
        if (result.isEmpty()) throw new RuntimeException("Error al crear el cierre de jornada");
        return mapToCierre(result.get(0));
    }

    @Override
    public Optional<CierreJornada> obtenerPorId(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id_cierre", "eq." + id);
        params.put("select", "*");
        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(mapToCierre(rows.get(0)));
    }

    @Override
    public Optional<CierreJornada> obtenerPorVendedorYFecha(UUID idVendedor, LocalDate fecha) {
        Map<String, String> params = new HashMap<>();
        params.put("id_vendedor", "eq." + idVendedor);
        params.put("fecha", "eq." + fecha);
        params.put("select", "*");
        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(mapToCierre(rows.get(0)));
    }

    @Override
    public List<CierreJornada> obtenerPorVendedor(UUID idVendedor) {
        Map<String, String> params = new HashMap<>();
        params.put("id_vendedor", "eq." + idVendedor);
        params.put("order", "fecha.desc");
        params.put("select", "*");
        return supabaseHttpClient.select(TABLE, params).stream()
                .map(this::mapToCierre).toList();
    }

    @Override
    public List<CierreJornada> obtenerTodos() {
        Map<String, String> params = new HashMap<>();
        params.put("order", "created_at.desc");
        params.put("select", "*");
        return supabaseHttpClient.select(TABLE, params).stream()
                .map(this::mapToCierre).toList();
    }

    @Override
    public CierreJornada actualizar(String id, CierreJornada cierre) {
        Map<String, String> params = new HashMap<>();
        params.put("id_cierre", "eq." + id);
        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, params, buildBody(cierre));
        if (result.isEmpty()) throw new RuntimeException("Cierre de jornada no encontrado: " + id);
        return mapToCierre(result.get(0));
    }

    @Override
    public void eliminar(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id_cierre", "eq." + id);
        supabaseHttpClient.deleteRows(TABLE, params);
    }

    @Override
    public boolean existePorVendedorYFecha(UUID idVendedor, LocalDate fecha) {
        Map<String, String> params = new HashMap<>();
        params.put("id_vendedor", "eq." + idVendedor);
        params.put("fecha", "eq." + fecha);
        params.put("select", "id_cierre");
        return !supabaseHttpClient.select(TABLE, params).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private Map<String, Object> buildBody(CierreJornada c) {
        Map<String, Object> body = new HashMap<>();
        body.put("id_vendedor", c.getIdVendedor() != null ? c.getIdVendedor().toString() : null);
        body.put("fecha", c.getFecha() != null ? c.getFecha().toString() : null);
        body.put("ventas_realizadas", c.getVentasRealizadas());
        body.put("total_efectivo", c.getTotalEfectivo());
        body.put("total_descuentos", c.getTotalDescuentos());
        body.put("stock_inicial_total", c.getStockInicialTotal());
        body.put("vendidos_total", c.getVendidosTotal());
        body.put("stock_final_total", c.getStockFinalTotal());
        body.put("estado_inventario", c.getEstadoInventario());
        body.put("dinero_esperado", c.getDineroEsperado());
        body.put("dinero_contado", c.getDineroContado());
        body.put("diferencia", c.getDiferencia());
        body.put("estado_efectivo", c.getEstadoEfectivo());
        body.put("estado", c.getEstado());
        return body;
    }

    private CierreJornada mapToCierre(Map<String, Object> row) {
        LocalDateTime createdAt = null;
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof String s) {
            try { createdAt = LocalDateTime.parse(s, ISO_FORMATTER); } catch (Exception ignored) {}
        }

        LocalDate fecha = null;
        Object fechaObj = row.get("fecha");
        if (fechaObj instanceof String s) {
            try { fecha = LocalDate.parse(s); } catch (Exception ignored) {}
        }

        UUID idVendedor = null;
        Object vendedorObj = row.get("id_vendedor");
        if (vendedorObj != null) {
            try { idVendedor = UUID.fromString(String.valueOf(vendedorObj)); } catch (Exception ignored) {}
        }

        return new CierreJornada(
                getString(row.get("id_cierre")),
                idVendedor,
                fecha,
                getInteger(row.get("ventas_realizadas")),
                getBigDecimal(row.get("total_efectivo")),
                getBigDecimal(row.get("total_descuentos")),
                getInteger(row.get("stock_inicial_total")),
                getInteger(row.get("vendidos_total")),
                getInteger(row.get("stock_final_total")),
                getString(row.get("estado_inventario")),
                getBigDecimal(row.get("dinero_esperado")),
                getBigDecimal(row.get("dinero_contado")),
                getBigDecimal(row.get("diferencia")),
                getString(row.get("estado_efectivo")),
                getString(row.get("estado")),
                createdAt
        );
    }

    private String getString(Object v) { return v == null ? null : String.valueOf(v); }

    private Integer getInteger(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof Double d) return d.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private BigDecimal getBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Double d) return BigDecimal.valueOf(d);
        if (v instanceof Integer i) return BigDecimal.valueOf(i);
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
