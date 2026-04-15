package com.back.puntoventa.app.domain.ruta.model;

import java.time.LocalDateTime;
public class Ubicacion {
    private Long id;
    private String idVendedor;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaRegistro;
    public Ubicacion() {}
    public Ubicacion(String idVendedor, Double latitud, Double longitud) {
        this.idVendedor = idVendedor;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaRegistro = LocalDateTime.now();
    }
    public String getIdVendedor() { return idVendedor; }
    public void setIdVendedor(String idVendedor) { this.idVendedor = idVendedor; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}