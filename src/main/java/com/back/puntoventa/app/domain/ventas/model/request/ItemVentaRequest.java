package com.back.puntoventa.app.domain.ventas.model.request;

/**
 * Request para un ítem de venta.
 */
public class ItemVentaRequest {
    private String idProducto;
    private Integer cantidad;
    private String tipoUnidad;

    public ItemVentaRequest() {}

    public ItemVentaRequest(String idProducto, Integer cantidad, String tipoUnidad) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.tipoUnidad = tipoUnidad;
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
}