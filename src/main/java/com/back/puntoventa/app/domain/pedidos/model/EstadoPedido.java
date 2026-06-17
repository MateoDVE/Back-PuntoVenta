package com.back.puntoventa.app.domain.pedidos.model;

/**
 * Representa los estados del ciclo de vida del pedido programado.
 */
public enum EstadoPedido {
    PENDIENTE,
    EN_RUTA,
    ENTREGADO,
    RECHAZADO,
    REPROGRAMADO
}
