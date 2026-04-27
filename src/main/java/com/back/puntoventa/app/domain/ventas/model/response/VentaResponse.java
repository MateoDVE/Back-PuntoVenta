package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response para una venta creada.
 */
public class VentaResponse {
    private String idVenta;
    private Integer idCliente;
    private UUID idVendedor;
    private LocalDateTime fechaHora;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal totalEfectivo;
    private String estado;
    private List<DetalleVentaResponse> detalles;

    public VentaResponse() {}

    public VentaResponse(String idVenta, Integer idCliente, UUID idVendedor, LocalDateTime fechaHora,
            BigDecimal subtotal, BigDecimal descuento, BigDecimal totalEfectivo, String estado,
            List<DetalleVentaResponse> detalles) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idVendedor = idVendedor;
        this.fechaHora = fechaHora;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalEfectivo = totalEfectivo;
        this.estado = estado;
        this.detalles = detalles;
    }

    // Getters and setters
    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public UUID getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(UUID idVendedor) {
        this.idVendedor = idVendedor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public void setTotalEfectivo(BigDecimal totalEfectivo) {
        this.totalEfectivo = totalEfectivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetalleVentaResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaResponse> detalles) {
        this.detalles = detalles;
    }
}