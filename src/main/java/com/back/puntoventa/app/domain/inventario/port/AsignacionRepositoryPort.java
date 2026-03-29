package com.back.puntoventa.app.domain.inventario.port;

import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: Define el contrato para persistir la carga de transporte.
 */
public interface AsignacionRepositoryPort {
    
    AsignacionStock guardar(AsignacionStock asignacion);

    List<AsignacionStock> obtenerPorVendedor(String idVendedor);

    Optional<AsignacionStock> obtenerPorId(String idCarga);

    AsignacionStock actualizarEstado(String idCarga, String nuevoEstado);
}
