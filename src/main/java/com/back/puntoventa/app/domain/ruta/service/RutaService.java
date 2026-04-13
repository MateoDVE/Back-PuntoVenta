package com.back.puntoventa.app.domain.ruta.service;

import com.back.puntoventa.app.domain.ruta.model.Ubicacion;
import com.back.puntoventa.app.domain.ruta.port.RutaRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service; // Importante para la inyección de dependencias
import java.util.List;

@Service // Esto le dice a Spring que esta clase es un componente de servicio
public class RutaService {

    private static final Logger logger = LoggerFactory.getLogger(RutaService.class);
    private final RutaRepositoryPort rutaRepositoryPort;

    public RutaService(RutaRepositoryPort rutaRepositoryPort) {
        this.rutaRepositoryPort = rutaRepositoryPort;
    }

    public void registrarSeguimiento(Ubicacion ubicacion) {
        logger.debug("Registrando seguimiento de ubicación para vendedor: {}", ubicacion.getIdVendedor());
        // Aquí podrías añadir la lógica del Sprint 2 más adelante:
        // if (vendedorTieneCargaValidada(ubicacion.getIdVendedor())) { ... }
        rutaRepositoryPort.guardarUbicacion(ubicacion);
        logger.info("Seguimiento registrado exitosamente para vendedor: {}", ubicacion.getIdVendedor());
    }

    // ESTE ES EL MÉTODO QUE FALTA Y POR ESO SALÍA ROJO EN EL REST ADAPTER
    public List<Ubicacion> obtenerHistorial(String idVendedor) {
        logger.info("Consultando historial de rutas para el vendedor: {}", idVendedor);
        return rutaRepositoryPort.obtenerHistorialPorVendedor(idVendedor);
    }
}