package com.back.puntoventa.app.domain.ruta.model;

import java.util.List;

public class Ruta {
    private String idVendedor;
    private List<Ubicacion> historialPuntos;
    private String estado; 

    public String getIdVendedor() {
        return idVendedor;
    }

    public List<Ubicacion> getHistorialPuntos() {
        return historialPuntos;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaEnMovimiento() {
        return true; 
    }
}