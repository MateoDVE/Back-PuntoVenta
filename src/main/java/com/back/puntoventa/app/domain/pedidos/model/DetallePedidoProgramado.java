package com.back.puntoventa.app.domain.pedidos.model;

/**
 * Entidad de dominio pura que representa el detalle de un Pedido Programado.
 */
public class DetallePedidoProgramado {
    private String id;
    private String idPedidoProgramado;
    private String idProducto;
    private Integer cantidad;

    public DetallePedidoProgramado() {}

    public DetallePedidoProgramado(String id, String idPedidoProgramado, String idProducto, Integer cantidad) {
        this.id = id;
        this.idPedidoProgramado = idPedidoProgramado;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdPedidoProgramado() {
        return idPedidoProgramado;
    }

    public void setIdPedidoProgramado(String idPedidoProgramado) {
        this.idPedidoProgramado = idPedidoProgramado;
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
