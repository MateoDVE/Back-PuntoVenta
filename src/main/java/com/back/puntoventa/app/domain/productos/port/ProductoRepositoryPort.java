package com.back.puntoventa.app.domain.productos.port;

import com.back.puntoventa.app.domain.productos.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Puerto: Contrato para persistencia de productos.
 * Permitirá cambiar la implementación (Supabase, BD local, etc.)
 */
public interface ProductoRepositoryPort {
    List<Producto> obtenerTodos();
    Optional<Producto> obtenerPorId(String id);
    Optional<Producto> obtenerPorSku(String sku);
    Producto crear(Producto producto);
    Producto actualizar(String id, Producto producto);
    void eliminar(String id);
    boolean existeSku(String sku, String excludeId);
}
