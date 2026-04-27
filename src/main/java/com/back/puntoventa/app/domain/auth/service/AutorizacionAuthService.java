package com.back.puntoventa.app.domain.auth.service;

import com.back.puntoventa.app.domain.auth.model.Credenciales;
import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.port.AutenticacionPort;
import com.back.puntoventa.app.domain.auth.port.UsuarioRepositoryPort;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso de autenticación.
 * Puro, sin dependencias a Spring ni Supabase.
 */
public class AutorizacionAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AutorizacionAuthService.class);
    private final AutenticacionPort autenticacionPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AutorizacionAuthService(AutenticacionPort autenticacionPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.autenticacionPort = autenticacionPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public Map<String, Object> autenticar(String email, String password) {
        logger.debug("Autenticando usuario con email: {}", email);
        Credenciales credenciales = new Credenciales(email, password);
        Map<String, Object> result = autenticacionPort.autenticar(credenciales);
        logger.debug("Autenticación completada para email: {}", email);
        return result;
    }

    public Usuario obtenerPerfilPorToken(String token) {
        logger.debug("Obteniendo perfil por token");
        Map<String, Object> userMap = autenticacionPort.obtenerUsuarioPorToken(token);
        Object userId = userMap.get("id");
        if (!(userId instanceof String id) || id.isBlank()) {
            logger.warn("Token inválido o sin ID de usuario");
            throw new DomainException("Token inválido");
        }

        Usuario usuario = usuarioRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con ID: {}", id);
                    return new DomainException("Usuario no encontrado");
                });
        logger.debug("Perfil obtenido exitosamente para usuario ID: {}", id);
        return usuario;
    }

    public Usuario obtenerPerfil(String userId) {
        logger.debug("Obteniendo perfil para usuario ID: {}", userId);
        Usuario usuario = usuarioRepositoryPort.obtenerPorId(userId)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con ID: {}", userId);
                    return new DomainException("Usuario no encontrado");
                });
        logger.debug("Perfil obtenido exitosamente para usuario ID: {}", userId);
        return usuario;
    }

    public void cerrarSesion(String userId) {
        logger.debug("Cerrando sesión para usuario ID: {}", userId);
        // La lógica aquí sería invalidar tokens, limpiar sesiones, etc.
        // Por ahora es un no-op a nivel de negocio
        logger.debug("Sesión cerrada para usuario ID: {}", userId);
    }
}
