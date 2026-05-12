package com.back.puntoventa.app.domain.ventas.model.response;

import java.math.BigDecimal;

public class ItemVentaCierreResponse {
    private String nombreProducto;
    private Integer cantidad;
    private String tipoUnidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public ItemVentaCierreResponse(String nombreProducto, Integer cantidad, String tipoUnidad,
                                   BigDecimal precioUnitario, BigDecimal subtotal) {
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.tipoUnidad = tipoUnidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public String getNombreProducto() { return nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public String getTipoUnidad() { return tipoUnidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
