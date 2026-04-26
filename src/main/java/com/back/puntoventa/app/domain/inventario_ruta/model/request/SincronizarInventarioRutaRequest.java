package com.back.puntoventa.app.domain.inventario_ruta.model.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request para sincronizar inventario de ruta.
 */
public record SincronizarInventarioRutaRequest(
        UUID vendedorId,
        LocalDate fecha,
        List<ItemSincronizacionRutaRequest> items
) {
}