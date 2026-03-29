package com.back.puntoventa.app.domain.inventario.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Representa la transferencia de stock a un camión.
 * Parte del Sprint 2: Logística de Salida.
 */
public class AsignacionStock {
    private final String idCarga;
    private final String idVendedor;
    private final String idProducto;
    private final Integer cantidadAsignada;
    private final String estadoValidacion; // "PENDIENTE" o "VALIDADO"
    private final LocalDateTime fechaAsignacion;

    public AsignacionStock(String idCarga, String idVendedor, String idProducto, 
                          Integer cantidadAsignada, String estadoValidacion, LocalDateTime fechaAsignacion) {
        
        if (cantidadAsignada == null || cantidadAsignada <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        this.idCarga = idCarga;
        this.idVendedor = idVendedor;
        this.idProducto = idProducto;
        this.cantidadAsignada = cantidadAsignada;
        this.estadoValidacion = estadoValidacion;
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getIdCarga() { return idCarga; }
    public String getIdVendedor() { return idVendedor; }
    public String getIdProducto() { return idProducto; }
    public Integer getCantidadAsignada() { return cantidadAsignada; }
    public String getEstadoValidacion() { return estadoValidacion; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
}