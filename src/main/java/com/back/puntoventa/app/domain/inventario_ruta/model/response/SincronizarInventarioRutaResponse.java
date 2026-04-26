package com.back.puntoventa.app.domain.inventario_ruta.model.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response para sincronizar inventario de ruta.
 */
public record SincronizarInventarioRutaResponse(
        UUID vendedorId,
        LocalDate fecha,
        Integer itemsActualizados,
        String estado
) {
}