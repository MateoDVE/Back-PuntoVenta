package com.back.puntoventa.app.application.pedidos;

import com.back.puntoventa.app.application.pedidos.dto.CreatePedidoProgramadoDto;
import com.back.puntoventa.app.application.pedidos.dto.DetallePedidoDto;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.pedidos.model.DetallePedidoProgramado;
import com.back.puntoventa.app.domain.pedidos.model.EstadoPedido;
import com.back.puntoventa.app.domain.pedidos.model.PedidoProgramado;
import com.back.puntoventa.app.domain.pedidos.model.PrioridadPedido;
import com.back.puntoventa.app.domain.pedidos.port.PedidoProgramadoRepositoryPort;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Aplicación para gestionar los pedidos programados y la reserva de stock (Preventa).
 */
@Service
public class PedidoProgramadoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoProgramadoService.class);

    private final ProductoRepositoryPort productoRepositoryPort;
    private final PedidoProgramadoRepositoryPort pedidoProgramadoRepositoryPort;

    public PedidoProgramadoService(ProductoRepositoryPort productoRepositoryPort,
                                   PedidoProgramadoRepositoryPort pedidoProgramadoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.pedidoProgramadoRepositoryPort = pedidoProgramadoRepositoryPort;
    }

    /**
     * Registra un nuevo pedido programado y realiza la reserva atómica de stock.
     * 
     * @param dto Datos del pedido.
     * @return El pedido programado registrado y guardado con sus detalles y su ID generado.
     */
    @Transactional
    public PedidoProgramado crearPedidoProgramado(CreatePedidoProgramadoDto dto) {
        logger.info("[PREVENTA] Iniciando procesamiento de registro para el pedido del cliente: {}", dto.getIdCliente());

        // 1. Validar la prioridad
        PrioridadPedido prioridad;
        try {
            prioridad = PrioridadPedido.valueOf(dto.getPrioridad().trim().toUpperCase());
        } catch (Exception e) {
            logger.warn("[PREVENTA] Validación de prioridad fallida: '{}'", dto.getPrioridad());
            throw new DomainException("La prioridad especificada no es válida. Debe ser ALTA, MEDIA o BAJA.");
        }

        // 2. Validación crítica de Stock (RF3) - Reserva Blanda
        for (DetallePedidoDto det : dto.getDetalles()) {
            String idProducto = det.getIdProducto();
            Integer cantidadSolicitada = det.getCantidad();

            Producto producto = productoRepositoryPort.obtenerPorId(idProducto)
                    .orElseThrow(() -> {
                        logger.warn("[PREVENTA] Validación de stock fallida - Producto no encontrado con ID: {}", idProducto);
                        return new DomainException("El producto con ID " + idProducto + " no existe en el catálogo.");
                    });

            Integer stockCentral = producto.getStockAlmacenCentral();
            if (stockCentral == null) {
                stockCentral = 0;
            }

            if (stockCentral < cantidadSolicitada) {
                logger.warn("[PREVENTA] Validación de stock fallida - Producto: '{}' (ID: {}), Stock disponible: {}, Stock solicitado: {}",
                        producto.getNombre(), idProducto, stockCentral, cantidadSolicitada);
                throw new DomainException("Stock insuficiente en Almacén Central para el producto '" + producto.getNombre()
                        + "'. Disponible actual: " + stockCentral);
            }
        }

        // 3. Crear cabecera del Pedido Programado en estado PENDIENTE
        PedidoProgramado nuevoPedido = new PedidoProgramado(
                null,
                dto.getIdCliente(),
                dto.getIdVendedor(),
                dto.getFechaProgramada(),
                EstadoPedido.PENDIENTE,
                prioridad,
                dto.getObservaciones(),
                LocalDateTime.now(),
                new ArrayList<>()
        );

        PedidoProgramado pedidoGuardado = pedidoProgramadoRepositoryPort.crear(nuevoPedido);
        logger.info("[PREVENTA] Cabecera del pedido programado persistida exitosamente - ID: {}", pedidoGuardado.getId());

        // 4. Crear detalles del Pedido Programado
        List<DetallePedidoProgramado> detallesList = new ArrayList<>();
        for (DetallePedidoDto detDto : dto.getDetalles()) {
            DetallePedidoProgramado detPedido = new DetallePedidoProgramado(
                    null,
                    pedidoGuardado.getId(),
                    detDto.getIdProducto(),
                    detDto.getCantidad()
            );
            detallesList.add(detPedido);
        }

        List<DetallePedidoProgramado> detallesGuardados = pedidoProgramadoRepositoryPort.crearDetalles(detallesList);
        pedidoGuardado.setDetalles(detallesGuardados);

        logger.info("[PREVENTA] Pedido programado procesado y registrado completamente con éxito - ID: {}", pedidoGuardado.getId());
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public List<PedidoProgramado> obtenerTodos() {
        logger.info("[PREVENTA] Listando todos los pedidos programados");
        return pedidoProgramadoRepositoryPort.obtenerTodos();
    }

    @Transactional(readOnly = true)
    public List<PedidoProgramado> obtenerPorVendedorYFecha(String idVendedor, LocalDate fecha) {
        logger.info("[PREVENTA] Consultando pedidos programados para vendedor: {} en fecha: {}", idVendedor, fecha);
        return pedidoProgramadoRepositoryPort.obtenerPorVendedorYFecha(idVendedor, fecha);
    }

    @Transactional
    public PedidoProgramado actualizarEstadoPedido(String id, String nuevoEstadoStr, LocalDate nuevaFecha) {
        logger.info("[PREVENTA] Actualizando estado del pedido programado ID: {} a {}", id, nuevoEstadoStr);
        PedidoProgramado pedido = pedidoProgramadoRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Pedido programado no encontrado con ID: " + id));

        EstadoPedido nuevoEstado;
        try {
            nuevoEstado = EstadoPedido.valueOf(nuevoEstadoStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new DomainException("El estado '" + nuevoEstadoStr + "' no es un estado válido para un pedido programado.");
        }

        pedido.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoPedido.REPROGRAMADO && nuevaFecha != null) {
            pedido.setFechaProgramada(nuevaFecha);
            // Si es reprogramado, volvemos a ponerlo en estado PENDIENTE para la nueva fecha
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }

        PedidoProgramado actualizado = pedidoProgramadoRepositoryPort.actualizar(pedido);
        logger.info("[PREVENTA] Pedido programado ID: {} actualizado exitosamente. Estado actual: {}", id, actualizado.getEstado());
        return actualizado;
    }
}
