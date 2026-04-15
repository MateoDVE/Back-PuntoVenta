package com.back.puntoventa.app.infrastructure.rest.dto;

public class UbicacionRequest {
    private String idVendedor;
    private Double latitud;
    private Double longitud;

    // Getters y Setters necesarios para que Spring lea el JSON
    public String getIdVendedor() { return idVendedor; }
    public void setIdVendedor(String idVendedor) { this.idVendedor = idVendedor; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}