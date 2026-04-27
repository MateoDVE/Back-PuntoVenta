package com.back.puntoventa.app.domain.inventario_ruta.model.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response para validar inventario de ruta.
 */
public record ValidarInventarioRutaResponse(
        UUID vendedorId,
        LocalDate fecha,
        String estado,
        Integer itemsValidados
) {
}