package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;

/**
 * Response para la conciliación de efectivo en el cierre de jornada.
 */
public record ConfirmarCierreResponse(
        BigDecimal dineroEsperado,
        BigDecimal dineroContado,
        BigDecimal diferencia,
        String estadoConciliacion
) {
}