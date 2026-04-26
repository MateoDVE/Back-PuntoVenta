package com.back.puntoventa.app.domain.ventas.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de dominio para carga de transporte.
 */
public class CargaTransporte {
    private String idCarga;
    private UUID idVendedor;
    private String idVehiculo;
    private String idProducto;
    private LocalDate fechaAsignacion;
    private Integer cantidadInicial;
    private Integer cantidadActual;
    private String estadoValidacion;

    public CargaTransporte(String idCarga, UUID idVendedor, String idVehiculo, String idProducto,
                           LocalDate fechaAsignacion, Integer cantidadInicial, Integer cantidadActual,
                           String estadoValidacion) {
        this.idCarga = idCarga;
        this.idVendedor = idVendedor;
        this.idVehiculo = idVehiculo;
        this.idProducto = idProducto;
        this.fechaAsignacion = fechaAsignacion;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.estadoValidacion = estadoValidacion;
    }

    // Getters
    public String getIdCarga() {
        return idCarga;
    }

    public UUID getIdVendedor() {
        return idVendedor;
    }

    public String getIdVehiculo() {
        return idVehiculo;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public Integer getCantidadActual() {
        return cantidadActual;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }
}