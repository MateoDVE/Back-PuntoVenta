package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen diario de ventas.
 */
public class ResumenDiarioResponse {
    private UUID idVendedor;
    private LocalDate fecha;
    private Integer cantidadVentas;
    private BigDecimal montoTotalVendido;
    private Integer totalProductosVendidos;
    private BigDecimal totalDescuentos;
    private BigDecimal dineroEsperado;

    public ResumenDiarioResponse() {
    }

    public ResumenDiarioResponse(UUID idVendedor, LocalDate fecha, Integer cantidadVentas, BigDecimal montoTotalVendido,
            Integer totalProductosVendidos, BigDecimal totalDescuentos, BigDecimal dineroEsperado) {
        this.idVendedor = idVendedor;
        this.fecha = fecha;
        this.cantidadVentas = cantidadVentas;
        this.montoTotalVendido = montoTotalVendido;
        this.totalProductosVendidos = totalProductosVendidos;
        this.totalDescuentos = totalDescuentos;
        this.dineroEsperado = dineroEsperado;
    }

    public UUID getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(UUID idVendedor) {
        this.idVendedor = idVendedor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getMontoTotalVendido() {
        return montoTotalVendido;
    }

    public void setMontoTotalVendido(BigDecimal montoTotalVendido) {
        this.montoTotalVendido = montoTotalVendido;
    }

    public Integer getTotalProductosVendidos() {
        return totalProductosVendidos;
    }

    public void setTotalProductosVendidos(Integer totalProductosVendidos) {
        this.totalProductosVendidos = totalProductosVendidos;
    }

    public BigDecimal getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(BigDecimal totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public BigDecimal getDineroEsperado() {
        return dineroEsperado;
    }

    public void setDineroEsperado(BigDecimal dineroEsperado) {
        this.dineroEsperado = dineroEsperado;
    }
}
