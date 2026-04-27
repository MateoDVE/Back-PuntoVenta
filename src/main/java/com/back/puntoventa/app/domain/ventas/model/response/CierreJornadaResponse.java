package com.back.puntoventa.app.domain.ventas.model.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de respuesta para cierre de jornada.
 */
public class CierreJornadaResponse {
    private UUID idVendedor;
    private LocalDate fecha;
    private ResumenFinancieroResponse resumenFinanciero;
    private ConciliacionInventarioResponse conciliacionInventario;

    public CierreJornadaResponse(UUID idVendedor, LocalDate fecha, ResumenFinancieroResponse resumenFinanciero,
                                 ConciliacionInventarioResponse conciliacionInventario) {
        this.idVendedor = idVendedor;
        this.fecha = fecha;
        this.resumenFinanciero = resumenFinanciero;
        this.conciliacionInventario = conciliacionInventario;
    }

    // Getters
    public UUID getIdVendedor() {
        return idVendedor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public ResumenFinancieroResponse getResumenFinanciero() {
        return resumenFinanciero;
    }

    public ConciliacionInventarioResponse getConciliacionInventario() {
        return conciliacionInventario;
    }
}