package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.clientes.model.Cliente;
import com.back.puntoventa.app.domain.clientes.port.ClienteRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SupabaseClienteRepositoryAdapter implements ClienteRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseClienteRepositoryAdapter.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE = "clientes";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseClienteRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public List<Cliente> obtenerTodos() {
        logger.debug("Obteniendo todos los clientes");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("order", "id_cliente.asc");
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return rows.stream()
                .map(this::mapToCliente)
                .toList();
    }

    @Override
    public Optional<Cliente> obtenerPorId(String id) {
        logger.debug("Obteniendo cliente por id={}", id);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_cliente", "eq." + id);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToCliente(rows.get(0)));
    }

    @Override
    public List<Cliente> obtenerPorVendedor(String idVendedor) {
        logger.debug("Obteniendo clientes por vendedorId={}", idVendedor);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_vendedor_creador", "eq." + idVendedor);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return rows.stream()
                .map(this::mapToCliente)
                .toList();
    }

    @Override
    public Optional<Cliente> obtenerPorCiNit(String ciNit) {
        logger.debug("Obteniendo cliente por ciNit={}", ciNit);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("ci_nit", "ilike." + ciNit);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToCliente(rows.get(0)));
    }

    @Override
    public Cliente crear(Cliente cliente) {
        logger.info("Insertando cliente en Supabase; negocio={}, ciNit={}", cliente.getNombreNegocio(),
                cliente.getCiNit());
        Map<String, Object> body = new HashMap<>();
        body.put("id_vendedor_creador", cliente.getIdVendedorCreador());
        body.put("nombre_negocio", cliente.getNombreNegocio());
        body.put("ci_nit", cliente.getCiNit());
        body.put("celular", cliente.getCelular());
        body.put("latitud", cliente.getLatitud());
        body.put("longitud", cliente.getLongitud());
        body.put("url_foto_fachada", cliente.getUrlFotoFachada());
        body.put("frecuencia_visita", cliente.getFrecuenciaVisita());
        body.put("estado", cliente.getEstado());

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, body, null, "return=representation");
        if (result.isEmpty()) {
            throw new RuntimeException("Error al crear cliente");
        }

        return mapToCliente(result.get(0));
    }

    @Override
    public Cliente actualizar(String id, Cliente cliente) {
        logger.info("Actualizando cliente en Supabase id={}", id);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_cliente", "eq." + id);

        Map<String, Object> body = new HashMap<>();
        body.put("nombre_negocio", cliente.getNombreNegocio());
        body.put("ci_nit", cliente.getCiNit());
        body.put("celular", cliente.getCelular());
        body.put("latitud", cliente.getLatitud());
        body.put("longitud", cliente.getLongitud());
        body.put("url_foto_fachada", cliente.getUrlFotoFachada());
        body.put("frecuencia_visita", cliente.getFrecuenciaVisita());
        body.put("estado", cliente.getEstado());

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        return mapToCliente(result.get(0));
    }

    @Override
    public void eliminar(String id) {
        logger.info("Eliminando cliente en Supabase id={}", id);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_cliente", "eq." + id);
        supabaseHttpClient.deleteRows(TABLE, queryParams);
    }

    @Override
    public boolean existeCiNit(String ciNit, String excludeId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("ci_nit", "ilike." + ciNit);
        if (excludeId != null) {
            queryParams.put("id_cliente", "neq." + excludeId);
        }
        queryParams.put("select", "id_cliente");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        return !rows.isEmpty();
    }

    private Cliente mapToCliente(Map<String, Object> row) {
        String id = getString(row.get("id_cliente"));
        String idVendedorCreador = getString(row.get("id_vendedor_creador"));
        String nombreNegocio = getString(row.get("nombre_negocio"));
        String ciNit = getString(row.get("ci_nit"));
        String celular = getString(row.get("celular"));
        Double latitud = getDoubleOrNull(row.get("latitud"));
        Double longitud = getDoubleOrNull(row.get("longitud"));
        String urlFotoFachada = getString(row.get("url_foto_fachada"));
        String frecuenciaVisita = getString(row.get("frecuencia_visita"));
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

        return new Cliente(
                id,
                idVendedorCreador,
                nombreNegocio,
                ciNit,
                celular,
                latitud,
                longitud,
                urlFotoFachada,
                frecuenciaVisita,
                estado,
                createdAt);
    }

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Double getDoubleOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double d) {
            return d;
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
}
