package com.back.puntoventa.app.domain.inventario_ruta.model.response;

/**
 * Item de inventario de ruta en response.
 */
public record ItemInventarioRutaResponse(
        Integer idCarga,
        String idProducto,
        String nombreProducto,
        Integer cantidadInicial,
        Integer cantidadActual,
        String estadoValidacion
) {
}