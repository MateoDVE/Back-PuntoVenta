package com.back.puntoventa.app.domain.auth.port;

import com.back.puntoventa.app.domain.auth.model.Usuario;
import java.util.Optional;

/**
 * Puerto de salida: Contrato para persistencia de usuarios.
 */
public interface UsuarioRepositoryPort {

    /**
     * Busca usuario por ID.
     */
    Optional<Usuario> obtenerPorId(String id);

    /**
     * Busca usuario por email.
     */
    Optional<Usuario> obtenerPorEmail(String email);

    /**
     * Crea o actualiza un usuario.
     */
    Usuario guardar(Usuario usuario);

    /**
     * Elimina un usuario por ID.
     */
    void eliminar(String id);

    /**
     * Verifica si un email ya existe.
     */
    boolean existeEmail(String email, String excludeUserId);
}
