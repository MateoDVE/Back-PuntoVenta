package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.auth.port.AutenticacionPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import com.back.puntoventa.app.domain.auth.model.Credenciales;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa AutenticacionPort usando Supabase.
 */
@Component
public class SupabaseAutenticacionAdapter implements AutenticacionPort {

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseAutenticacionAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public Map<String, Object> autenticar(Credenciales credenciales) {
        return supabaseHttpClient.signIn(credenciales.getEmail(), credenciales.getPassword());
    }

    @Override
    public Map<String, Object> obtenerUsuarioPorToken(String token) {
        return supabaseHttpClient.getUserByToken(token);
    }

    @Override
    public Map<String, Object> crearUsuarioAdmin(String email, String password, String nombre, String rol) {
        return supabaseHttpClient.createUserByAdmin(email, password, nombre, rol);
    }

    @Override
    public Map<String, Object> actualizarUsuarioAdmin(String userId, Map<String, Object> payload) {
        return supabaseHttpClient.updateUserByAdmin(userId, payload);
    }

    @Override
    public void eliminarUsuarioAdmin(String userId) {
        supabaseHttpClient.deleteUserByAdmin(userId);
    }
}
