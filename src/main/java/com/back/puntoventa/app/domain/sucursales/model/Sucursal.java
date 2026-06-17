package com.back.puntoventa.app.domain.sucursales.model;

import java.time.LocalDateTime;

public final class Sucursal {

    private final String id;
    private final String nombre;
    private final Double latitud;
    private final Double longitud;
    private final Boolean esPrincipal;
    private final LocalDateTime fechaCreacion;

    public Sucursal(String id,
            String nombre,
            Double latitud,
            Double longitud,
            Boolean esPrincipal,
            LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.esPrincipal = esPrincipal;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
