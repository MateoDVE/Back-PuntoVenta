package com.back.puntoventa.app.domain.auth.service;

import com.back.puntoventa.app.domain.auth.model.Credenciales;
import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.port.AutenticacionPort;
import com.back.puntoventa.app.domain.auth.port.UsuarioRepositoryPort;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.util.Map;

/**
 * Caso de uso de autenticación.
 * Puro, sin dependencias a Spring ni Supabase.
 */
public class AutorizacionAuthService {

    private final AutenticacionPort autenticacionPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AutorizacionAuthService(AutenticacionPort autenticacionPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.autenticacionPort = autenticacionPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public Map<String, Object> autenticar(String email, String password) {
        Credenciales credenciales = new Credenciales(email, password);
        return autenticacionPort.autenticar(credenciales);
    }

    public Usuario obtenerPerfilPorToken(String token) {
        Map<String, Object> userMap = autenticacionPort.obtenerUsuarioPorToken(token);
        Object userId = userMap.get("id");
        if (!(userId instanceof String id) || id.isBlank()) {
            throw new DomainException("Token inválido");
        }

        return usuarioRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    public Usuario obtenerPerfil(String userId) {
        return usuarioRepositoryPort.obtenerPorId(userId)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    public void cerrarSesion(String userId) {
        // La lógica aquí sería invalidar tokens, limpiar sesiones, etc.
        // Por ahora es un no-op a nivel de negocio
    }
}
