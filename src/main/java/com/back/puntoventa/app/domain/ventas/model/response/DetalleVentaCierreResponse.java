package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de respuesta para detalle de venta en cierre.
 */
public class DetalleVentaCierreResponse {
    private String idVenta;
    private LocalDateTime fechaHora;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal totalEfectivo;
    private String estado;

    public DetalleVentaCierreResponse(String idVenta, LocalDateTime fechaHora, BigDecimal subtotal,
                                      BigDecimal descuento, BigDecimal totalEfectivo, String estado) {
        this.idVenta = idVenta;
        this.fechaHora = fechaHora;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalEfectivo = totalEfectivo;
        this.estado = estado;
    }

    // Getters
    public String getIdVenta() {
        return idVenta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public String getEstado() {
        return estado;
    }
}