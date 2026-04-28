package com.back.puntoventa.app.application.inventario_ruta;

import com.back.puntoventa.app.domain.inventario_ruta.model.request.SincronizarInventarioRutaRequest;
import com.back.puntoventa.app.domain.inventario_ruta.model.request.ValidarInventarioRutaRequest;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.InventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.SincronizarInventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.model.response.ValidarInventarioRutaResponse;
import com.back.puntoventa.app.domain.inventario_ruta.service.GestionInventarioRutaService;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.common.ApiException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de inventario de ruta al mundo exterior.
 */
@RestController
@RequestMapping("/inventario-ruta")
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioRutaRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InventarioRutaRestAdapter.class);
    private final GestionInventarioRutaService gestionInventarioRutaService;

    public InventarioRutaRestAdapter(GestionInventarioRutaService gestionInventarioRutaService) {
        this.gestionInventarioRutaService = gestionInventarioRutaService;
    }

    @GetMapping
    public ResponseEntity<InventarioRutaResponse> obtenerInventarioRuta(
            @RequestParam UUID vendedorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("GET /inventario-ruta - Obtener inventario de ruta para vendedor {} y fecha {}", vendedorId, fecha);
        try {
            InventarioRutaResponse response = gestionInventarioRutaService.obtenerInventarioRuta(vendedorId, fecha);
            logger.info("Inventario de ruta obtenido: {} items", response.items().size());
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al obtener inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al obtener inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al obtener inventario de ruta", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<SincronizarInventarioRutaResponse> sincronizarInventarioRuta(@RequestBody SincronizarInventarioRutaRequest request) {
        logger.info("POST /inventario-ruta/sincronizar - Sincronizar inventario de ruta para vendedor {} y fecha {}", request.vendedorId(), request.fecha());
        try {
            SincronizarInventarioRutaResponse response = gestionInventarioRutaService.sincronizarInventarioRuta(request);
            logger.info("Sincronización completada: {} items actualizados", response.itemsActualizados());
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al sincronizar inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al sincronizar inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al sincronizar inventario de ruta", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidarInventarioRutaResponse> validarInventarioRuta(@RequestBody ValidarInventarioRutaRequest request) {
        logger.info("POST /inventario-ruta/validar - Validar inventario de ruta para vendedor {} y fecha {}", request.vendedorId(), request.fecha());
        try {
            ValidarInventarioRutaResponse response = gestionInventarioRutaService.validarInventarioRuta(request);
            logger.info("Validación completada: {} items validados", response.itemsValidados());
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al validar inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al validar inventario de ruta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al validar inventario de ruta", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}