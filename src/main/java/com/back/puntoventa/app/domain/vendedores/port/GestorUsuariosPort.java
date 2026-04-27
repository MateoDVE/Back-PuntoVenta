package com.back.puntoventa.app.domain.vendedores.port;

import java.util.Map;

/**
 * Puerto de salida: Contrato para gestión de usuarios en autenticación.
 */
public interface GestorUsuariosPort {

    /**
     * Crea un usuario administrador en el proveedor de autenticación.
     */
    Map<String, Object> crearUsuario(String email, String password, String nombre);

    /**
     * Actualiza un usuario.
     */
    Map<String, Object> actualizarUsuario(String userId, Map<String, Object> payload);

    /**
     * Elimina un usuario.
     */
    void eliminarUsuario(String userId);
}
