package com.back.puntoventa.app.application.inventario.dto;

public class AsignarStockDto {
    private String idVendedor;
    private String idProducto;
    private Integer cantidad;

    public AsignarStockDto() {}

    // Getters y Setters
    public String getIdVendedor() { return idVendedor; }
    public void setIdVendedor(String idVendedor) { this.idVendedor = idVendedor; }
    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
