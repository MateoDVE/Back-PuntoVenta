package com.back.puntoventa.app.domain.sucursales.service;

import com.back.puntoventa.app.domain.sucursales.model.Sucursal;
import com.back.puntoventa.app.domain.sucursales.port.SucursalRepositoryPort;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GestionSucursalesService {

    private static final Logger logger = LoggerFactory.getLogger(GestionSucursalesService.class);
    private final SucursalRepositoryPort sucursalRepositoryPort;

    public GestionSucursalesService(SucursalRepositoryPort sucursalRepositoryPort) {
        this.sucursalRepositoryPort = sucursalRepositoryPort;
    }

    public List<Sucursal> obtenerTodas() {
        logger.debug("Obteniendo todas las sucursales");
        return sucursalRepositoryPort.obtenerTodas();
    }

    public Sucursal obtenerPorId(String id) {
        logger.debug("Obteniendo sucursal por ID: {}", id);
        return sucursalRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Sucursal no encontrada"));
    }

    public Sucursal crearSucursal(String nombre, Double latitud, Double longitud, Boolean esPrincipal) {
        logger.debug("Creando sucursal - Nombre: {}, esPrincipal: {}", nombre, esPrincipal);

        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre de la sucursal es obligatorio");
        }
        if (latitud == null || longitud == null) {
            throw new DomainException("La latitud y longitud son obligatorias");
        }

        boolean principal = esPrincipal != null && esPrincipal;

        if (principal) {
            desactivarOtrosPrincipales(null);
        }

        Sucursal sucursal = new Sucursal(
                null,
                nombre.trim(),
                latitud,
                longitud,
                principal,
                LocalDateTime.now()
        );

        Sucursal creada = sucursalRepositoryPort.crear(sucursal);
        logger.info("Sucursal creada exitosamente - ID: {}, Nombre: {}", creada.getId(), creada.getNombre());
        return creada;
    }

    public Sucursal actualizarSucursal(String id, String nombre, Double latitud, Double longitud, Boolean esPrincipal) {
        logger.debug("Actualizando sucursal - ID: {}", id);

        Sucursal actual = obtenerPorId(id);

        String nombreFinal = actual.getNombre();
        if (nombre != null && !nombre.isBlank()) {
            nombreFinal = nombre.trim();
        }

        Double latitudFinal = latitud != null ? latitud : actual.getLatitud();
        Double longitudFinal = longitud != null ? longitud : actual.getLongitud();
        boolean esPrincipalFinal = esPrincipal != null ? esPrincipal : actual.getEsPrincipal();

        if (esPrincipalFinal && !actual.getEsPrincipal()) {
            desactivarOtrosPrincipales(id);
        }

        Sucursal sucursalActualizada = new Sucursal(
                id,
                nombreFinal,
                latitudFinal,
                longitudFinal,
                esPrincipalFinal,
                actual.getFechaCreacion()
        );

        Sucursal actualizada = sucursalRepositoryPort.actualizar(id, sucursalActualizada);
        logger.info("Sucursal actualizada exitosamente - ID: {}", id);
        return actualizada;
    }

    public void eliminarSucursal(String id) {
        logger.debug("Eliminando sucursal - ID: {}", id);
        Sucursal actual = obtenerPorId(id);
        if (actual.getEsPrincipal()) {
            // Verificar si hay otras sucursales
            List<Sucursal> todas = sucursalRepositoryPort.obtenerTodas();
            if (todas.size() > 1) {
                throw new DomainException("No se puede eliminar la sucursal principal si existen otras sucursales. Asigne otra sucursal como principal primero.");
            }
        }
        sucursalRepositoryPort.eliminar(id);
        logger.info("Sucursal eliminada exitosamente - ID: {}", id);
    }

    private void desactivarOtrosPrincipales(String excluirId) {
        List<Sucursal> sucursales = sucursalRepositoryPort.obtenerTodas();
        for (Sucursal s : sucursales) {
            if (s.getEsPrincipal() && (excluirId == null || !s.getId().equals(excluirId))) {
                Sucursal sucursalModificada = new Sucursal(
                        s.getId(),
                        s.getNombre(),
                        s.getLatitud(),
                        s.getLongitud(),
                        false,
                        s.getFechaCreacion()
                );
                sucursalRepositoryPort.actualizar(s.getId(), sucursalModificada);
            }
        }
    }
}
