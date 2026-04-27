package com.back.puntoventa.app.domain.vendedores.port;

import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: Contrato para persistencia de vendedores.
 */
public interface VendedorRepositoryPort {

    /**
     * Obtiene todos los vendedores.
     */
    List<Vendedor> obtenerTodos();

    /**
     * Obtiene un vendedor por ID.
     */
    Optional<Vendedor> obtenerPorId(String id);

    /**
     * Obtiene vendedor por email.
     */
    Optional<Vendedor> obtenerPorEmail(String email);

    /**
     * Crea un nuevo vendedor.
     */
    Vendedor crear(Vendedor vendedor);

    /**
     * Actualiza un vendedor.
     */
    Vendedor actualizar(String id, Vendedor vendedor);

    /**
     * Elimina un vendedor.
     */
    void eliminar(String id);

    /**
     * Verifica si email ya existe.
     */
    boolean existeEmail(String email, String excludeId);
}
