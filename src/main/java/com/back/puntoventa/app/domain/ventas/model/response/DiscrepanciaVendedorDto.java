package com.back.puntoventa.app.domain.ventas.model.response;

public record DiscrepanciaVendedorDto(
    String nombre,
    int stockEsperado,
    int stockActual,
    int diferencia,
    boolean correcto
) {}
