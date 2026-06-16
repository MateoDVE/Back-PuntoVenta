package com.back.puntoventa.app.domain.pedidos.port;

import com.back.puntoventa.app.domain.pedidos.model.DetallePedidoProgramado;
import com.back.puntoventa.app.domain.pedidos.model.PedidoProgramado;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: Contrato para la persistencia de pedidos programados.
 */
public interface PedidoProgramadoRepositoryPort {
    PedidoProgramado crear(PedidoProgramado pedido);
    List<DetallePedidoProgramado> crearDetalles(List<DetallePedidoProgramado> detalles);
    Optional<PedidoProgramado> obtenerPorId(String id);
    List<DetallePedidoProgramado> obtenerDetallesPorPedidoId(String idPedido);
}
