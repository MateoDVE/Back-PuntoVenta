package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.vendedores.port.GestorUsuariosPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa GestorUsuariosPort usando Supabase.
 */
@Component
public class SupabaseGestorUsuariosAdapter implements GestorUsuariosPort {

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseGestorUsuariosAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public Map<String, Object> crearUsuario(String email, String password, String nombre) {
        return supabaseHttpClient.createUserByAdmin(email, password, nombre, "VENDEDOR");
    }

    @Override
    public Map<String, Object> actualizarUsuario(String userId, Map<String, Object> payload) {
        return supabaseHttpClient.updateUserByAdmin(userId, payload);
    }

    @Override
    public void eliminarUsuario(String userId) {
        supabaseHttpClient.deleteUserByAdmin(userId);
    }
}
