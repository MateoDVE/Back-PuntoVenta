package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo de respuesta para resumen financiero.
 */
public class ResumenFinancieroResponse {
    private Integer ventasRealizadas;
    private BigDecimal totalEfectivo;
    private BigDecimal totalDescuentos;
    private List<DetalleVentaCierreResponse> detalleVentas;

    public ResumenFinancieroResponse(Integer ventasRealizadas, BigDecimal totalEfectivo, BigDecimal totalDescuentos,
                                     List<DetalleVentaCierreResponse> detalleVentas) {
        this.ventasRealizadas = ventasRealizadas;
        this.totalEfectivo = totalEfectivo;
        this.totalDescuentos = totalDescuentos;
        this.detalleVentas = detalleVentas;
    }

    // Getters
    public Integer getVentasRealizadas() {
        return ventasRealizadas;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public BigDecimal getTotalDescuentos() {
        return totalDescuentos;
    }

    public List<DetalleVentaCierreResponse> getDetalleVentas() {
        return detalleVentas;
    }
}