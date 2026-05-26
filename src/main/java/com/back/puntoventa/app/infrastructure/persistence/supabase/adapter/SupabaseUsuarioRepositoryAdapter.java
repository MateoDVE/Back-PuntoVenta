package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.port.UsuarioRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa UsuarioRepositoryPort usando Supabase.
 */
@Component
public class SupabaseUsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final SupabaseHttpClient supabaseHttpClient;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public SupabaseUsuarioRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public Optional<Usuario> obtenerPorId(String id) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario,nombre,email,rol,estado,created_at");
        query.put("id_usuario", "eq." + id);
        query.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return rows.stream().findFirst().map(this::mapToUsuario);
    }

    @Override
    public Optional<Usuario> obtenerPorEmail(String email) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario,nombre,email,rol,estado,created_at");
        query.put("email", "ilike." + email);
        query.put("is_delete", "eq.false");

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return rows.stream().findFirst().map(this::mapToUsuario);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        Map<String, Object> row = new HashMap<>();
        row.put("id_usuario", usuario.getId());
        row.put("nombre", usuario.getNombre());
        row.put("email", usuario.getEmail());
        row.put("rol", usuario.getRol());
        row.put("estado", usuario.getEstado());

        List<Map<String, Object>> result = supabaseHttpClient.insert(
                "usuarios",
                List.of(row),
                "id_usuario",
                "resolution=merge-duplicates,return=representation"
        );

        return result.stream().findFirst().map(this::mapToUsuario)
                .orElse(usuario);
    }

    @Override
    public void eliminar(String id) {
        Map<String, String> query = new HashMap<>();
        query.put("id_usuario", "eq." + id);
        
        Map<String, Object> body = new HashMap<>();
        body.put("is_delete", true);
        
        supabaseHttpClient.update("usuarios", query, body);
    }

    @Override
    public boolean existeEmail(String email, String excludeUserId) {
        Map<String, String> query = new HashMap<>();
        query.put("select", "id_usuario");
        query.put("email", "ilike." + email);
        query.put("is_delete", "eq.false");

        if (excludeUserId != null) {
            query.put("id_usuario", "neq." + excludeUserId);
        }

        List<Map<String, Object>> rows = supabaseHttpClient.select("usuarios", query);
        return !rows.isEmpty();
    }

    private Usuario mapToUsuario(Map<String, Object> row) {
        String id = (String) row.get("id_usuario");
        String nombre = (String) row.get("nombre");
        String email = (String) row.get("email");
        String rol = (String) row.get("rol");
        String estado = (String) row.get("estado");

        LocalDateTime createdAt = null;
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof String createdAtStr) {
            createdAt = LocalDateTime.parse(createdAtStr, ISO_FORMATTER);
        }

        return new Usuario(id, nombre, email, rol, estado, createdAt);
    }
}
