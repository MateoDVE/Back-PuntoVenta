package com.back.puntoventa.app.domain.ventas.model.response;

import java.util.List;

/**
 * Modelo de respuesta para conciliación de inventario.
 */
public class ConciliacionInventarioResponse {
    private Integer stockInicialTotal;
    private Integer vendidosTotal;
    private Integer stockFinalTotal;
    private String estado;
    private List<DetalleProductoConciliacionResponse> detalleProductos;

    public ConciliacionInventarioResponse(Integer stockInicialTotal, Integer vendidosTotal, Integer stockFinalTotal,
                                          String estado, List<DetalleProductoConciliacionResponse> detalleProductos) {
        this.stockInicialTotal = stockInicialTotal;
        this.vendidosTotal = vendidosTotal;
        this.stockFinalTotal = stockFinalTotal;
        this.estado = estado;
        this.detalleProductos = detalleProductos;
    }

    // Getters
    public Integer getStockInicialTotal() {
        return stockInicialTotal;
    }

    public Integer getVendidosTotal() {
        return vendidosTotal;
    }

    public Integer getStockFinalTotal() {
        return stockFinalTotal;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetalleProductoConciliacionResponse> getDetalleProductos() {
        return detalleProductos;
    }
}