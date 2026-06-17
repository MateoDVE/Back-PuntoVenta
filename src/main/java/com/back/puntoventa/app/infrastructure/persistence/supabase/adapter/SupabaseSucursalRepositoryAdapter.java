package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.sucursales.model.Sucursal;
import com.back.puntoventa.app.domain.sucursales.port.SucursalRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SupabaseSucursalRepositoryAdapter implements SucursalRepositoryPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE = "sucursales";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseSucursalRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public List<Sucursal> obtenerTodas() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("order", "nombre.asc");
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return rows.stream()
                .map(this::mapToSucursal)
                .toList();
    }

    @Override
    public Optional<Sucursal> obtenerPorId(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToSucursal(rows.get(0)));
    }

    @Override
    public Sucursal crear(Sucursal sucursal) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", sucursal.getNombre());
        body.put("latitud", sucursal.getLatitud());
        body.put("longitud", sucursal.getLongitud());
        body.put("es_principal", sucursal.getEsPrincipal());

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, body, null, "return=representation");
        if (result.isEmpty()) {
            throw new RuntimeException("Error al crear sucursal");
        }

        return mapToSucursal(result.get(0));
    }

    @Override
    public Sucursal actualizar(String id, Sucursal sucursal) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", sucursal.getNombre());
        body.put("latitud", sucursal.getLatitud());
        body.put("longitud", sucursal.getLongitud());
        body.put("es_principal", sucursal.getEsPrincipal());

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Sucursal no encontrada");
        }

        return mapToSucursal(result.get(0));
    }

    @Override
    public void eliminar(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);
        supabaseHttpClient.deleteRows(TABLE, queryParams);
    }

    private Sucursal mapToSucursal(Map<String, Object> row) {
        String id = getString(row.get("id"));
        String nombre = getString(row.get("nombre"));
        Double latitud = getDouble(row.get("latitud"));
        Double longitud = getDouble(row.get("longitud"));
        Boolean esPrincipal = getBoolean(row.get("es_principal"));

        LocalDateTime fechaCreacion = null;
        Object fechaCreacionObj = row.get("fecha_creacion");
        if (fechaCreacionObj instanceof String fechaCreacionStr) {
            try {
                fechaCreacion = LocalDateTime.parse(fechaCreacionStr, ISO_FORMATTER);
            } catch (Exception e) {
                // Ignorar formato inválido
            }
        }

        return new Sucursal(
                id,
                nombre,
                latitud,
                longitud,
                esPrincipal,
                fechaCreacion
        );
    }

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Double getDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double d) {
            return d;
        }
        if (value instanceof Float f) {
            return f.doubleValue();
        }
        if (value instanceof Integer i) {
            return i.doubleValue();
        }
        if (value instanceof Long l) {
            return l.doubleValue();
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

    private Boolean getBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}
