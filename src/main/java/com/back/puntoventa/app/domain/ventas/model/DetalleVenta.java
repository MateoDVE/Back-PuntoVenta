package com.back.puntoventa.app.domain.ventas.model;

import java.math.BigDecimal;

/**
 * Entidad del dominio: Detalle de Venta.
 * Representa cada ítem de una venta.
 */
public final class DetalleVenta {
    private final String idDetalle;
    private final String idVenta;
    private final String idProducto;
    private final Integer cantidad;
    private final String tipoUnidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleVenta(String idDetalle, String idVenta, String idProducto, Integer cantidad,
            String tipoUnidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        if (idVenta == null || idVenta.isBlank()) {
            throw new IllegalArgumentException("ID venta no puede estar vacío");
        }
        if (idProducto == null || idProducto.isBlank()) {
            throw new IllegalArgumentException("ID producto no puede estar vacío");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a 0");
        }
        if (tipoUnidad == null || tipoUnidad.isBlank()) {
            throw new IllegalArgumentException("Tipo unidad no puede estar vacío");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Precio unitario debe ser mayor a 0");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal debe ser mayor o igual a 0");
        }

        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.tipoUnidad = tipoUnidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public String getIdDetalle() {
        return idDetalle;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getTipoUnidad() {
        return tipoUnidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}