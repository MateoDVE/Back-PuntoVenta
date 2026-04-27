package com.back.puntoventa.app.domain.inventario_ruta.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.inventario_ruta.model.CargaTransporte;
import com.back.puntoventa.app.domain.inventario_ruta.model.request.SincronizarInventarioRutaRequest;
import com.back.puntoventa.app.domain.inventario_ruta.model.request.ValidarInventarioRutaRequest;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.InventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.ItemInventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.SincronizarInventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.ValidarInventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.port.InventarioRutaRepositoryPort;
import com.back.puntoventa.app.domain.productos.model.Producto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: Gestión de inventario de ruta.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionInventarioRutaService {

    private static final Logger logger = LoggerFactory.getLogger(GestionInventarioRutaService.class);
    private final InventarioRutaRepositoryPort inventarioRutaRepositoryPort;

    public GestionInventarioRutaService(InventarioRutaRepositoryPort inventarioRutaRepositoryPort) {
        this.inventarioRutaRepositoryPort = inventarioRutaRepositoryPort;
    }

    public InventarioRutaResponse obtenerInventarioRuta(UUID idVendedor, LocalDate fecha) {
        logger.info("Obteniendo inventario de ruta para vendedor {} en fecha {}", idVendedor, fecha);
        if (idVendedor == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }

        validarVendedor(idVendedor);

        List<CargaTransporte> cargas = inventarioRutaRepositoryPort.obtenerCargasPorVendedorYFecha(idVendedor, fecha);
        List<String> idsProductos = cargas.stream()
                .map(CargaTransporte::getIdProducto)
                .distinct()
                .toList();

        List<Producto> productos = inventarioRutaRepositoryPort.obtenerProductosPorIds(idsProductos);
        Map<String, Producto> productosMap = productos.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        List<ItemInventarioRutaResponse> items = cargas.stream()
                .map(carga -> {
                    Producto producto = productosMap.get(carga.getIdProducto());
                    String nombreProducto = producto != null ? producto.getNombre() : "Producto desconocido";
                    return new ItemInventarioRutaResponse(
                            carga.getIdCarga(),
                            carga.getIdProducto(),
                            nombreProducto,
                            carga.getCantidadInicial(),
                            carga.getCantidadActual(),
                            carga.getEstadoValidacion()
                    );
                })
                .toList();

        logger.info("Inventario de ruta obtenido: {} items", items.size());
        return new InventarioRutaResponse(idVendedor, fecha, items);
    }

    public SincronizarInventarioRutaResponse sincronizarInventarioRuta(SincronizarInventarioRutaRequest request) {
        logger.info("Sincronizando inventario de ruta para vendedor {} en fecha {}", request.vendedorId(), request.fecha());
        if (request.vendedorId() == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (request.fecha() == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Items no puede estar vacío");
        }

        validarVendedor(request.vendedorId());

        int itemsActualizados = 0;
        for (var item : request.items()) {
            if (item.cantidadActual() < 0) {
                throw new IllegalArgumentException("Cantidad actual no puede ser negativa");
            }
            inventarioRutaRepositoryPort.actualizarCantidadActual(item.idCarga(), item.cantidadActual());
            itemsActualizados++;
        }

        logger.info("Sincronización completada: {} items actualizados", itemsActualizados);
        return new SincronizarInventarioRutaResponse(request.vendedorId(), request.fecha(), itemsActualizados, "SINCRONIZADO");
    }

    public ValidarInventarioRutaResponse validarInventarioRuta(ValidarInventarioRutaRequest request) {
        logger.info("Validando inventario de ruta para vendedor {} en fecha {}", request.vendedorId(), request.fecha());
        if (request.vendedorId() == null) {
            throw new IllegalArgumentException("ID de vendedor es obligatorio");
        }
        if (request.fecha() == null) {
            throw new IllegalArgumentException("Fecha es obligatoria");
        }

        validarVendedor(request.vendedorId());

        List<CargaTransporte> cargas = inventarioRutaRepositoryPort.obtenerCargasPorVendedorYFecha(request.vendedorId(), request.fecha());

        boolean todasValidas = cargas.stream()
                .allMatch(carga -> carga.getCantidadActual() >= 0);

        if (!todasValidas) {
            logger.warn("Datos inconsistentes en inventario de ruta para vendedor {}", request.vendedorId());
            throw new DomainException("Datos inconsistentes en inventario de ruta");
        }

        List<Integer> idsCargas = cargas.stream()
                .map(CargaTransporte::getIdCarga)
                .toList();

        inventarioRutaRepositoryPort.validarCargas(idsCargas);

        logger.info("Validación completada: {} items validados", idsCargas.size());
        return new ValidarInventarioRutaResponse(request.vendedorId(), request.fecha(), "VALIDADO", idsCargas.size());
    }

    private void validarVendedor(UUID idVendedor) {
        if (!inventarioRutaRepositoryPort.vendedorExiste(idVendedor)) {
            logger.warn("Vendedor no encontrado: {}", idVendedor);
            throw new DomainException("Vendedor no encontrado");
        }
    }
}