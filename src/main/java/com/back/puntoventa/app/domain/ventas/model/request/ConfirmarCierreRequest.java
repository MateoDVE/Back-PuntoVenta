package com.back.puntoventa.app.domain.ventas.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para confirmar el cierre de jornada con conciliación de efectivo.
 */
public record ConfirmarCierreRequest(
        UUID idVendedor,
        LocalDate fecha,
        BigDecimal dineroContado
) {
}