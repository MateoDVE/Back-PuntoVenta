package com.back.puntoventa.app.domain.ventas.model.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request para crear una venta.
 */
public class CrearVentaRequest {
    private Integer idCliente;
    private UUID idVendedor;
    private BigDecimal descuento;
    private List<ItemVentaRequest> items;

    public CrearVentaRequest() {}

    public CrearVentaRequest(Integer idCliente, UUID idVendedor, BigDecimal descuento, List<ItemVentaRequest> items) {
        this.idCliente = idCliente;
        this.idVendedor = idVendedor;
        this.descuento = descuento;
        this.items = items;
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

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public List<ItemVentaRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemVentaRequest> items) {
        this.items = items;
    }
}