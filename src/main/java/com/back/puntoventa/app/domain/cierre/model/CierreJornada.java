package com.back.puntoventa.app.domain.cierre.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio: Cierre de jornada persistido.
 */
public class CierreJornada {

    private String idCierre;
    private UUID idVendedor;
    private LocalDate fecha;

    // Resumen financiero
    private Integer ventasRealizadas;
    private BigDecimal totalEfectivo;
    private BigDecimal totalDescuentos;

    // Conciliación de inventario
    private Integer stockInicialTotal;
    private Integer vendidosTotal;
    private Integer stockFinalTotal;
    private String estadoInventario;

    // Conciliación de efectivo
    private BigDecimal dineroEsperado;
    private BigDecimal dineroContado;
    private BigDecimal diferencia;
    private String estadoEfectivo;

    private String estado;
    private LocalDateTime createdAt;

    public CierreJornada() {}

    public CierreJornada(String idCierre, UUID idVendedor, LocalDate fecha,
                         Integer ventasRealizadas, BigDecimal totalEfectivo, BigDecimal totalDescuentos,
                         Integer stockInicialTotal, Integer vendidosTotal, Integer stockFinalTotal,
                         String estadoInventario, BigDecimal dineroEsperado, BigDecimal dineroContado,
                         BigDecimal diferencia, String estadoEfectivo, String estado, LocalDateTime createdAt) {
        this.idCierre = idCierre;
        this.idVendedor = idVendedor;
        this.fecha = fecha;
        this.ventasRealizadas = ventasRealizadas;
        this.totalEfectivo = totalEfectivo;
        this.totalDescuentos = totalDescuentos;
        this.stockInicialTotal = stockInicialTotal;
        this.vendidosTotal = vendidosTotal;
        this.stockFinalTotal = stockFinalTotal;
        this.estadoInventario = estadoInventario;
        this.dineroEsperado = dineroEsperado;
        this.dineroContado = dineroContado;
        this.diferencia = diferencia;
        this.estadoEfectivo = estadoEfectivo;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public void validate() {
        if (idVendedor == null) throw new IllegalArgumentException("El ID del vendedor es obligatorio");
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        if (ventasRealizadas == null || ventasRealizadas < 0) throw new IllegalArgumentException("Las ventas realizadas deben ser >= 0");
        if (totalEfectivo == null || totalEfectivo.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El total efectivo debe ser >= 0");
        if (dineroContado == null || dineroContado.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El dinero contado debe ser >= 0");
    }

    // Getters y setters
    public String getIdCierre() { return idCierre; }
    public void setIdCierre(String idCierre) { this.idCierre = idCierre; }

    public UUID getIdVendedor() { return idVendedor; }
    public void setIdVendedor(UUID idVendedor) { this.idVendedor = idVendedor; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Integer getVentasRealizadas() { return ventasRealizadas; }
    public void setVentasRealizadas(Integer ventasRealizadas) { this.ventasRealizadas = ventasRealizadas; }

    public BigDecimal getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(BigDecimal totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public BigDecimal getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(BigDecimal totalDescuentos) { this.totalDescuentos = totalDescuentos; }

    public Integer getStockInicialTotal() { return stockInicialTotal; }
    public void setStockInicialTotal(Integer stockInicialTotal) { this.stockInicialTotal = stockInicialTotal; }

    public Integer getVendidosTotal() { return vendidosTotal; }
    public void setVendidosTotal(Integer vendidosTotal) { this.vendidosTotal = vendidosTotal; }

    public Integer getStockFinalTotal() { return stockFinalTotal; }
    public void setStockFinalTotal(Integer stockFinalTotal) { this.stockFinalTotal = stockFinalTotal; }

    public String getEstadoInventario() { return estadoInventario; }
    public void setEstadoInventario(String estadoInventario) { this.estadoInventario = estadoInventario; }

    public BigDecimal getDineroEsperado() { return dineroEsperado; }
    public void setDineroEsperado(BigDecimal dineroEsperado) { this.dineroEsperado = dineroEsperado; }

    public BigDecimal getDineroContado() { return dineroContado; }
    public void setDineroContado(BigDecimal dineroContado) { this.dineroContado = dineroContado; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }

    public String getEstadoEfectivo() { return estadoEfectivo; }
    public void setEstadoEfectivo(String estadoEfectivo) { this.estadoEfectivo = estadoEfectivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
