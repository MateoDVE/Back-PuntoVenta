package com.back.puntoventa.app.domain.inventario.model;

import java.time.LocalDateTime;
public class AsignacionStock {
    private final String idCarga;
    private final String idVendedor;
    private final String idProducto;
    private final Integer cantidadAsignada;
    private final Integer cantidadActual;
    private final String estadoValidacion;
    private final LocalDateTime fechaAsignacion;

    public AsignacionStock(String idCarga, String idVendedor, String idProducto, 
                          Integer cantidadAsignada, Integer cantidadActual, String estadoValidacion, LocalDateTime fechaAsignacion) {
        
        if (cantidadAsignada == null || cantidadAsignada <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        this.idCarga = idCarga;
        this.idVendedor = idVendedor;
        this.idProducto = idProducto;
        this.cantidadAsignada = cantidadAsignada;
        this.cantidadActual = cantidadActual != null ? cantidadActual : cantidadAsignada;
        this.estadoValidacion = estadoValidacion;
        this.fechaAsignacion = fechaAsignacion;
    }

    public AsignacionStock(String idCarga, String idVendedor, String idProducto, 
                          Integer cantidadAsignada, String estadoValidacion, LocalDateTime fechaAsignacion) {
        this(idCarga, idVendedor, idProducto, cantidadAsignada, cantidadAsignada, estadoValidacion, fechaAsignacion);
    }

    public String getIdCarga() { return idCarga; }
    public String getIdVendedor() { return idVendedor; }
    public String getIdProducto() { return idProducto; }
    public Integer getCantidadAsignada() { return cantidadAsignada; }
    public Integer getCantidadActual() { return cantidadActual; }
    public String getEstadoValidacion() { return estadoValidacion; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
}