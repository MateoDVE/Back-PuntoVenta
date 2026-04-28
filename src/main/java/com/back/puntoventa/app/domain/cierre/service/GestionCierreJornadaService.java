package com.back.puntoventa.app.domain.cierre.service;

import com.back.puntoventa.app.domain.cierre.model.CierreJornada;
import com.back.puntoventa.app.domain.cierre.model.request.RegistrarCierreRequest;
import com.back.puntoventa.app.domain.cierre.port.CierreJornadaRepositoryPort;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: Gestión de cierres de jornada.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionCierreJornadaService {

    private static final Logger logger = LoggerFactory.getLogger(GestionCierreJornadaService.class);

    private final CierreJornadaRepositoryPort cierreRepositoryPort;

    public GestionCierreJornadaService(CierreJornadaRepositoryPort cierreRepositoryPort) {
        this.cierreRepositoryPort = cierreRepositoryPort;
    }

    public CierreJornada registrarCierre(RegistrarCierreRequest request) {
        logger.info("Registrando cierre de jornada para vendedor {} en fecha {}", request.getIdVendedor(), request.getFecha());

        if (request.getIdVendedor() == null) throw new IllegalArgumentException("El ID del vendedor es obligatorio");
        if (request.getFecha() == null) throw new IllegalArgumentException("La fecha es obligatoria");

        if (cierreRepositoryPort.existePorVendedorYFecha(request.getIdVendedor(), request.getFecha())) {
            logger.warn("Ya existe un cierre para vendedor {} en fecha {}", request.getIdVendedor(), request.getFecha());
            throw new DomainException("Ya existe un cierre registrado para este vendedor en la fecha indicada");
        }

        CierreJornada cierre = new CierreJornada(
                null,
                request.getIdVendedor(),
                request.getFecha(),
                request.getVentasRealizadas() != null ? request.getVentasRealizadas() : 0,
                request.getTotalEfectivo() != null ? request.getTotalEfectivo() : BigDecimal.ZERO,
                request.getTotalDescuentos() != null ? request.getTotalDescuentos() : BigDecimal.ZERO,
                request.getStockInicialTotal() != null ? request.getStockInicialTotal() : 0,
                request.getVendidosTotal() != null ? request.getVendidosTotal() : 0,
                request.getStockFinalTotal() != null ? request.getStockFinalTotal() : 0,
                request.getEstadoInventario() != null ? request.getEstadoInventario() : "CORRECTO",
                request.getDineroEsperado() != null ? request.getDineroEsperado() : BigDecimal.ZERO,
                request.getDineroContado() != null ? request.getDineroContado() : BigDecimal.ZERO,
                request.getDiferencia() != null ? request.getDiferencia() : BigDecimal.ZERO,
                request.getEstadoEfectivo() != null ? request.getEstadoEfectivo() : "CORRECTO",
                "CONFIRMADO",
                null
        );

        cierre.validate();

        CierreJornada guardado = cierreRepositoryPort.crear(cierre);
        logger.info("Cierre registrado con ID: {}", guardado.getIdCierre());
        return guardado;
    }

    public CierreJornada obtenerPorId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El ID del cierre es obligatorio");

        return cierreRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Cierre de jornada no encontrado: " + id));
    }

    public List<CierreJornada> obtenerPorVendedor(UUID idVendedor) {
        if (idVendedor == null) throw new IllegalArgumentException("El ID del vendedor es obligatorio");
        return cierreRepositoryPort.obtenerPorVendedor(idVendedor);
    }

    public CierreJornada obtenerPorVendedorYFecha(UUID idVendedor, LocalDate fecha) {
        if (idVendedor == null) throw new IllegalArgumentException("El ID del vendedor es obligatorio");
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");

        return cierreRepositoryPort.obtenerPorVendedorYFecha(idVendedor, fecha)
                .orElseThrow(() -> new DomainException("No se encontró cierre para ese vendedor y fecha"));
    }

    public List<CierreJornada> obtenerTodos() {
        return cierreRepositoryPort.obtenerTodos();
    }

    public CierreJornada actualizarCierre(String id, RegistrarCierreRequest request) {
        logger.info("Actualizando cierre de jornada ID: {}", id);

        CierreJornada existente = obtenerPorId(id);

        CierreJornada actualizado = new CierreJornada(
                existente.getIdCierre(),
                existente.getIdVendedor(),
                existente.getFecha(),
                request.getVentasRealizadas() != null ? request.getVentasRealizadas() : existente.getVentasRealizadas(),
                request.getTotalEfectivo() != null ? request.getTotalEfectivo() : existente.getTotalEfectivo(),
                request.getTotalDescuentos() != null ? request.getTotalDescuentos() : existente.getTotalDescuentos(),
                request.getStockInicialTotal() != null ? request.getStockInicialTotal() : existente.getStockInicialTotal(),
                request.getVendidosTotal() != null ? request.getVendidosTotal() : existente.getVendidosTotal(),
                request.getStockFinalTotal() != null ? request.getStockFinalTotal() : existente.getStockFinalTotal(),
                request.getEstadoInventario() != null ? request.getEstadoInventario() : existente.getEstadoInventario(),
                request.getDineroEsperado() != null ? request.getDineroEsperado() : existente.getDineroEsperado(),
                request.getDineroContado() != null ? request.getDineroContado() : existente.getDineroContado(),
                request.getDiferencia() != null ? request.getDiferencia() : existente.getDiferencia(),
                request.getEstadoEfectivo() != null ? request.getEstadoEfectivo() : existente.getEstadoEfectivo(),
                existente.getEstado(),
                existente.getCreatedAt()
        );

        return cierreRepositoryPort.actualizar(id, actualizado);
    }

    public void eliminarCierre(String id) {
        logger.info("Eliminando cierre de jornada ID: {}", id);
        obtenerPorId(id);
        cierreRepositoryPort.eliminar(id);
    }
}
