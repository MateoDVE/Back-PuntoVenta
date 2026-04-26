package com.back.puntoventa.app.domain.inventario_ruta.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de dominio para carga de transporte en inventario de ruta.
 */
public class CargaTransporte {
    private Integer idCarga;
    private UUID idVendedor;
    private Integer idVehiculo;
    private String idProducto;
    private LocalDate fechaAsignacion;
    private Integer cantidadInicial;
    private Integer cantidadActual;
    private String estadoValidacion;

    public CargaTransporte(Integer idCarga, UUID idVendedor, Integer idVehiculo, String idProducto,
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
    public Integer getIdCarga() {
        return idCarga;
    }

    public UUID getIdVendedor() {
        return idVendedor;
    }

    public Integer getIdVehiculo() {
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

    // Setters
    public void setCantidadActual(Integer cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }
}