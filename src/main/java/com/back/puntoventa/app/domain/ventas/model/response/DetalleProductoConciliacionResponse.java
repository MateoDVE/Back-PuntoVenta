package com.back.puntoventa.app.domain.ventas.model.response;

/**
 * Modelo de respuesta para detalle de producto en conciliación.
 */
public class DetalleProductoConciliacionResponse {
    private String idProducto;
    private String nombre;
    private Integer stockInicial;
    private Integer vendido;
    private Integer esperado;
    private Integer actual;

    public DetalleProductoConciliacionResponse(String idProducto, String nombre, Integer stockInicial,
                                               Integer vendido, Integer esperado, Integer actual) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.stockInicial = stockInicial;
        this.vendido = vendido;
        this.esperado = esperado;
        this.actual = actual;
    }

    // Getters
    public String getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public Integer getVendido() {
        return vendido;
    }

    public Integer getEsperado() {
        return esperado;
    }

    public Integer getActual() {
        return actual;
    }
}