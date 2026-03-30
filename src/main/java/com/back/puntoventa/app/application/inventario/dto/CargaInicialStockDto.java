package com.back.puntoventa.app.application.inventario.dto;

public class CargaInicialStockDto {
    private String idProducto;
    private Integer cantidad;

    public CargaInicialStockDto() {
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
}
