package com.back.puntoventa.app.domain.cierre.model.request;

import java.math.BigDecimal;

/**
 * Request para registrar la liquidación de una jornada.
 * Contiene el monto de dinero físico recibido por el administrador.
 */
public class LiquidarJornadaRequest {

    private BigDecimal dineroRecibido;

    public LiquidarJornadaRequest() {}

    public BigDecimal getDineroRecibido() {
        return dineroRecibido;
    }

    public void setDineroRecibido(BigDecimal dineroRecibido) {
        this.dineroRecibido = dineroRecibido;
    }
}
