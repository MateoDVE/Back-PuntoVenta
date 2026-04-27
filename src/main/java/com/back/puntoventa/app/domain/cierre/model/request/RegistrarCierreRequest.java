package com.back.puntoventa.app.domain.cierre.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para registrar un cierre de jornada.
 * El frontend envía los datos calculados por GET /ventas/cierre-jornada
 * junto con el dinero contado físicamente por el vendedor.
 */
public class RegistrarCierreRequest {

    private UUID idVendedor;
    private LocalDate fecha;

    // Resumen financiero (viene de CierreJornadaResponse)
    private Integer ventasRealizadas;
    private BigDecimal totalEfectivo;
    private BigDecimal totalDescuentos;

    // Conciliación de inventario
    private Integer stockInicialTotal;
    private Integer vendidosTotal;
    private Integer stockFinalTotal;
    private String estadoInventario;

    // Conciliación de efectivo (viene de ConfirmarCierreResponse)
    private BigDecimal dineroEsperado;
    private BigDecimal dineroContado;
    private BigDecimal diferencia;
    private String estadoEfectivo;

    public RegistrarCierreRequest() {}

    // Getters y setters
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
}
