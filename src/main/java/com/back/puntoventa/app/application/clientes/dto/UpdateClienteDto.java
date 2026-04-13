package com.back.puntoventa.app.application.clientes.dto;

public class UpdateClienteDto {

    private String nombreNegocio;
    private String ciNit;
    private String celular;
    private Double latitud;
    private Double longitud;
    private String urlFotoFachada;
    private String frecuenciaVisita;
    private String estado;

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = nombreNegocio;
    }

    public String getCiNit() {
        return ciNit;
    }

    public void setCiNit(String ciNit) {
        this.ciNit = ciNit;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getUrlFotoFachada() {
        return urlFotoFachada;
    }

    public void setUrlFotoFachada(String urlFotoFachada) {
        this.urlFotoFachada = urlFotoFachada;
    }

    public String getFrecuenciaVisita() {
        return frecuenciaVisita;
    }

    public void setFrecuenciaVisita(String frecuenciaVisita) {
        this.frecuenciaVisita = frecuenciaVisita;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
