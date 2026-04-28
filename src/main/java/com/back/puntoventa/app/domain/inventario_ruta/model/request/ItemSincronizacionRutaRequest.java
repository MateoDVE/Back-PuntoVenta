package com.back.puntoventa.app.domain.inventario_ruta.model.request;

/**
 * Item para sincronización de inventario de ruta.
 */
public record ItemSincronizacionRutaRequest(
        Integer idCarga,
        String idProducto,
        Integer cantidadActual
) {
}