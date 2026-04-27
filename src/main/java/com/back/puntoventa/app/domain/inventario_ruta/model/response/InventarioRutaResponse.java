package com.back.puntoventa.app.domain.inventario_ruta.model.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response para obtener inventario de ruta.
 */
public record InventarioRutaResponse(
        UUID vendedorId,
        LocalDate fecha,
        List<ItemInventarioRutaResponse> items
) {
}