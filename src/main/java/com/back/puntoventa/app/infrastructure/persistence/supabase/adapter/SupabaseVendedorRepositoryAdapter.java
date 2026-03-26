package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import com.back.puntoventa.app.domain.vendedores.port.VendedorRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa VendedorRepositoryPort usando Supabase.
 */
@Component
public class SupabaseVendedorRepositoryAdapter implements VendedorRepositoryPort {

    private final SupabaseHttpClient supabaseHttpClient;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public SupabaseVendedorRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public List<Vendedor> obtenerTodos() {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario,nombre,email,estado,created_at");
        query.put("rol", "eq.VENDEDOR");
        query.put("order", "id_usuario.asc");

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return rows.stream().map(this::mapToVendedor).toList();
    }

    @Override
    public Optional<Vendedor> obtenerPorId(String id) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario,nombre,email,estado,created_at");
        query.put("id_usuario", "eq." + id);
        query.put("rol", "eq.VENDEDOR");

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return rows.stream().findFirst().map(this::mapToVendedor);
    }

    @Override
    public Optional<Vendedor> obtenerPorEmail(String email) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario,nombre,email,estado,created_at");
        query.put("email", "ilike." + email);
        query.put("rol", "eq.VENDEDOR");

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return rows.stream().findFirst().map(this::mapToVendedor);
    }

    @Override
    public Vendedor crear(Vendedor vendedor) {
        Map<String, Object> row = new HashMap<>();
        row.put("id_usuario", vendedor.getId());
        row.put("nombre", vendedor.getNombre());
        row.put("email", vendedor.getEmail());
        row.put("rol", "VENDEDOR");
        row.put("estado", vendedor.getEstado());

        List<Map<String, Object>> result = supabaseHttpClient.insert(
                "usuarios",
                List.of(row),
                "id_usuario",
                "resolution=merge-duplicates,return=representation"
        );

        return result.stream().findFirst().map(this::mapToVendedor)
                .orElse(vendedor);
    }

    @Override
    public Vendedor actualizar(String id, Vendedor vendedor) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", vendedor.getNombre());
        updates.put("email", vendedor.getEmail());
        updates.put("estado", vendedor.getEstado());

        Map<String, String> query = new HashMap<>();
        query.put("id_usuario", "eq." + id);
        query.put("rol", "eq.VENDEDOR");

        List<Map<String, Object>> result = supabaseHttpClient.update("usuarios", query, updates);
        return result.stream().findFirst().map(this::mapToVendedor)
                .orElse(vendedor);
    }

    @Override
    public void eliminar(String id) {
        Map<String, String> query = new HashMap<>();
        query.put("id_usuario", "eq." + id);
        query.put("rol", "eq.VENDEDOR");
        supabaseHttpClient.deleteRows("usuarios", query);
    }

    @Override
    public boolean existeEmail(String email, String excludeId) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario");
        query.put("email", "ilike." + email);
        query.put("rol", "eq.VENDEDOR");

        if (excludeId != null) {
            query.put("id_usuario", "neq." + excludeId);
        }

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return !rows.isEmpty();
    }

    private Vendedor mapToVendedor(Map<String, Object> row) {
        String id = (String) row.get("id_usuario");
        String nombre = (String) row.get("nombre");
        String email = (String) row.get("email");
        String estado = (String) row.get("estado");

        LocalDateTime createdAt = null;
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof String createdAtStr) {
            createdAt = LocalDateTime.parse(createdAtStr, ISO_FORMATTER);
        }

        return new Vendedor(id, nombre, email, estado, createdAt);
    }
}
