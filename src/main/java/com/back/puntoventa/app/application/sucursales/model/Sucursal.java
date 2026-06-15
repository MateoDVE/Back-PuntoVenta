package com.back.puntoventa.app.application.sucursales.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sucursal {
    private String id;
    private String nombre;
    private Double latitud;
    private Double longitud;
    
    @JsonProperty("esPrincipal")
    private boolean esPrincipal;
    private String fechaCreacion;

    public Sucursal() {}

    public Sucursal(String id, String nombre, Double latitud, Double longitud, boolean esPrincipal, String fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.esPrincipal = esPrincipal;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public boolean isEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
