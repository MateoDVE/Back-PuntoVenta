package com.back.puntoventa.app.application.ventas;

import com.back.puntoventa.app.domain.ventas.model.request.CrearVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResponse;
import com.back.puntoventa.app.domain.ventas.service.GestionVentasService;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de
 * ventas del dominio al mundo exterior.
 */
@RestController
@RequestMapping("/ventas")
@CrossOrigin(origins = "http://localhost:4200")
public class VentaRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(VentaRestAdapter.class);
    private final GestionVentasService gestionVentasService;

    public VentaRestAdapter(GestionVentasService gestionVentasService) {
        this.gestionVentasService = gestionVentasService;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> crearVenta(@RequestBody CrearVentaRequest request) {
        logger.info("POST /ventas - Crear nueva venta");
        try {
            VentaResponse response = gestionVentasService.crearVenta(request);
            logger.info("Venta creada exitosamente - ID: {}", response.getIdVenta());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al crear venta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al crear venta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al crear venta", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}