package com.back.puntoventa.app.domain.auth.port;

import com.back.puntoventa.app.domain.auth.model.Credenciales;
import java.util.Map;

/**
 * Puerto de salida: Contrato para autenticación contra proveedor externo.
 */
public interface AutenticacionPort {

    /**
     * Autentica un usuario con email y contraseña.
     * @return Map con access_token, refresh_token, etc.
     */
    Map<String, Object> autenticar(Credenciales credenciales);

    /**
     * Obtiene el usuario autenticado desde un token.
     */
    Map<String, Object> obtenerUsuarioPorToken(String token);

    /**
     * Crea un usuario administrador (solo backend).
     */
    Map<String, Object> crearUsuarioAdmin(String email, String password, String nombre, String rol);

    /**
     * Actualiza un usuario administrador.
     */
    Map<String, Object> actualizarUsuarioAdmin(String userId, Map<String, Object> payload);

    /**
     * Elimina un usuario administrador.
     */
    void eliminarUsuarioAdmin(String userId);
}
