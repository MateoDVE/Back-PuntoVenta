package com.back.puntoventa.app.domain.inventario.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.port.AsignacionRepositoryPort;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GestionInventarioService {

    private static final Logger logger = LoggerFactory.getLogger(GestionInventarioService.class);

    private final AsignacionRepositoryPort asignacionRepository;
    private final ProductoRepositoryPort productoRepository;

    public GestionInventarioService(AsignacionRepositoryPort asignacionRepository,
            ProductoRepositoryPort productoRepository) {
        this.asignacionRepository = asignacionRepository;
        this.productoRepository = productoRepository;
    }

    public AsignacionStock asignarStockAVendedor(String productoId, String vendedorId, Integer cantidad) {
        logger.info("Asignando stock - Producto: {}, Vendedor: {}, Cantidad: {}", productoId, vendedorId, cantidad);
        if (cantidad == null || cantidad <= 0) {
            logger.warn("Cantidad inválida para asignación: {}", cantidad);
            throw new DomainException("La cantidad asignada debe ser mayor a cero");
        }

        Producto producto = productoRepository.obtenerPorId(productoId)
                .orElseThrow(() -> new DomainException("Producto no encontrado"));

        Integer stockActual = producto.getStockAlmacenCentral() == null ? 0 : producto.getStockAlmacenCentral();
        if (stockActual < cantidad) {
            logger.warn("Stock insuficiente - Producto: {}, Disponible: {}, Solicitado: {}", productoId, stockActual,
                    cantidad);
            throw new DomainException(
                    "Stock insuficiente en almacén central. Disponible: " + stockActual);
        }

        Integer nuevoStockCentral = stockActual - cantidad;
        Producto productoActualizado = new Producto(
                producto.getId(),
                producto.getSku(),
                producto.getIdCategoria(),
                producto.getNombre(),
                producto.getPrecioUnidad(),
                producto.getPrecioCaja(),
                producto.getUnidadesPorCaja(),
                nuevoStockCentral,
                producto.getDescripcion(),
                producto.getUrlImagen(),
                producto.getEstado(),
                producto.getCreatedAt());
        productoRepository.actualizar(productoId, productoActualizado);
        logger.info("Stock central descontado - Producto: {}, Stock anterior: {}, Stock nuevo: {}", productoId,
                stockActual, nuevoStockCentral);

        AsignacionStock nuevaAsignacion = new AsignacionStock(
                null,
                vendedorId,
                productoId,
                cantidad,
                "PENDIENTE",
                LocalDateTime.now());
        AsignacionStock asignacionGuardada = asignacionRepository.guardar(nuevaAsignacion);
        logger.info("Asignación creada - idCarga: {}, Estado: {}", asignacionGuardada.getIdCarga(),
                asignacionGuardada.getEstadoValidacion());
        return asignacionGuardada;
    }

    public Producto cargarStockInicialAlmacen(String productoId, Integer cantidad) {
        logger.info("Carga inicial de stock - Producto: {}, Cantidad a sumar: {}", productoId, cantidad);
        if (cantidad == null || cantidad <= 0) {
            logger.warn("Cantidad inválida para carga inicial: {}", cantidad);
            throw new DomainException("La cantidad de carga inicial debe ser mayor a cero");
        }

        Producto producto = productoRepository.obtenerPorId(productoId)
                .orElseThrow(() -> new DomainException("Producto no encontrado"));

        Integer stockActual = producto.getStockAlmacenCentral() == null ? 0 : producto.getStockAlmacenCentral();
        Integer nuevoStock = stockActual + cantidad;

        Producto productoActualizado = new Producto(
                producto.getId(),
                producto.getSku(),
                producto.getIdCategoria(),
                producto.getNombre(),
                producto.getPrecioUnidad(),
                producto.getPrecioCaja(),
                producto.getUnidadesPorCaja(),
                nuevoStock,
                producto.getDescripcion(),
                producto.getUrlImagen(),
                producto.getEstado(),
                producto.getCreatedAt());

        Producto resultado = productoRepository.actualizar(productoId, productoActualizado);
        logger.info("Carga inicial aplicada - Producto: {}, Stock anterior: {}, Stock nuevo: {}", productoId,
                stockActual, nuevoStock);
        return resultado;
    }

    public List<AsignacionStock> obtenerCargasPorVendedor(String vendedorId) {
        logger.info("Consultando cargas por vendedor: {}", vendedorId);
        return asignacionRepository.obtenerPorVendedor(vendedorId);
    }

    public AsignacionStock confirmarValidacionAdmin(String idCarga) {
        logger.info("Validación de admin para carga: {}", idCarga);
        AsignacionStock asignacion = asignacionRepository.obtenerPorId(idCarga)
                .orElseThrow(() -> new DomainException("No se encontró la carga de transporte especificada"));

        String estado = normalizarEstado(asignacion.getEstadoValidacion());
        if ("VALIDADO".equals(estado)) {
            throw new DomainException("Esta carga ya fue validada completamente");
        }
        if ("VALIDADO_ADMIN".equals(estado)) {
            throw new DomainException("La validación de administrador ya fue registrada");
        }
        if (!"PENDIENTE".equals(estado)) {
            throw new DomainException("Estado de carga no válido para validación de admin: " + estado);
        }

        AsignacionStock actualizada = asignacionRepository.actualizarEstado(idCarga, "VALIDADO_ADMIN");
        logger.info("Carga validada por admin - idCarga: {}", idCarga);
        return actualizada;
    }

    public AsignacionStock confirmarSalidaInventario(String idCarga, String idVendedorSolicitante) {
        logger.info("Confirmación de salida por vendedor - idCarga: {}, vendedor: {}", idCarga,
                idVendedorSolicitante);
        AsignacionStock asignacion = asignacionRepository.obtenerPorId(idCarga)
                .orElseThrow(() -> new DomainException("No se encontró la carga de transporte especificada"));

        if (!asignacion.getIdVendedor().equals(idVendedorSolicitante)) {
            logger.warn("Intento de confirmar carga de otro vendedor - Carga vendedor: {}, Solicitante: {}",
                    asignacion.getIdVendedor(), idVendedorSolicitante);
            throw new DomainException("No tienes permisos para confirmar esta carga");
        }

        String estado = normalizarEstado(asignacion.getEstadoValidacion());
        if ("VALIDADO".equals(estado)) {
            throw new DomainException("Esta carga ya fue validada previamente y está en ruta");
        }
        if (!"VALIDADO_ADMIN".equals(estado)) {
            throw new DomainException("La carga aún no fue validada por administrador");
        }

        AsignacionStock actualizada = asignacionRepository.actualizarEstado(idCarga, "VALIDADO");
        logger.info("Carga validada por vendedor y confirmada en salida - idCarga: {}", idCarga);
        return actualizada;
    }

    private String normalizarEstado(String estado) {
        return estado == null ? "" : estado.trim().toUpperCase();
    }
}