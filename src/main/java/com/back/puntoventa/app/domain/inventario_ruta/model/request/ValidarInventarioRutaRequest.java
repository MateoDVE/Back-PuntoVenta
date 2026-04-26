package com.back.puntoventa.app.domain.inventario_ruta.model.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para validar inventario de ruta.
 */
public record ValidarInventarioRutaRequest(
        UUID vendedorId,
        LocalDate fecha
) {
}