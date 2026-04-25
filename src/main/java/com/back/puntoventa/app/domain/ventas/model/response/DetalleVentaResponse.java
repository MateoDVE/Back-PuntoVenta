package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;

/**
 * Response para detalle de venta.
 */
public class DetalleVentaResponse {
    private String idDetalle;
    private String idProducto;
    private Integer cantidad;
    private String tipoUnidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleVentaResponse() {}

    public DetalleVentaResponse(String idDetalle, String idProducto, Integer cantidad,
            String tipoUnidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalle = idDetalle;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.tipoUnidad = tipoUnidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Getters and setters
    public String getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(String idDetalle) {
        this.idDetalle = idDetalle;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoUnidad() {
        return tipoUnidad;
    }

    public void setTipoUnidad(String tipoUnidad) {
        this.tipoUnidad = tipoUnidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}