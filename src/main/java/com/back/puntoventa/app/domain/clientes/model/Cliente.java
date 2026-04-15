package com.back.puntoventa.app.domain.clientes.model;

import java.time.LocalDateTime;

public final class Cliente {

    private final String id;
    private final String idVendedorCreador;
    private final String nombreNegocio;
    private final String ciNit;
    private final String celular;
    private final Double latitud;
    private final Double longitud;
    private final String urlFotoFachada;
    private final String frecuenciaVisita;
    private final String estado;
    private final LocalDateTime createdAt;

    public Cliente(String id,
            String idVendedorCreador,
            String nombreNegocio,
            String ciNit,
            String celular,
            Double latitud,
            Double longitud,
            String urlFotoFachada,
            String frecuenciaVisita,
            String estado,
            LocalDateTime createdAt) {
        this.id = id;
        this.idVendedorCreador = idVendedorCreador;
        this.nombreNegocio = nombreNegocio;
        this.ciNit = ciNit;
        this.celular = celular;
        this.latitud = latitud;
        this.longitud = longitud;
        this.urlFotoFachada = urlFotoFachada;
        this.frecuenciaVisita = frecuenciaVisita;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getIdVendedorCreador() {
        return idVendedorCreador;
    }

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public String getCiNit() {
        return ciNit;
    }

    public String getCelular() {
        return celular;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public String getUrlFotoFachada() {
        return urlFotoFachada;
    }

    public String getFrecuenciaVisita() {
        return frecuenciaVisita;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
