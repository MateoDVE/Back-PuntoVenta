package com.back.puntoventa.app.application.sucursales.controller;

import com.back.puntoventa.app.application.sucursales.dto.CreateSucursalDto;
import com.back.puntoventa.app.domain.sucursales.model.Sucursal;
import com.back.puntoventa.app.domain.sucursales.service.GestionSucursalesService;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sucursales")
@CrossOrigin(origins = "http://localhost:4200")
public class SucursalesRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SucursalesRestAdapter.class);
    private final GestionSucursalesService gestionSucursalesService;

    public SucursalesRestAdapter(GestionSucursalesService gestionSucursalesService) {
        this.gestionSucursalesService = gestionSucursalesService;
    }

    @GetMapping
    public ResponseEntity<List<Sucursal>> obtenerSucursales() {
        logger.info("GET /sucursales - Obteniendo todas las sucursales");
        try {
            List<Sucursal> sucursales = gestionSucursalesService.obtenerTodas();
            return ResponseEntity.ok(sucursales);
        } catch (Exception ex) {
            logger.error("Error al obtener sucursales", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener las sucursales");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerSucursalPorId(@PathVariable String id) {
        logger.info("GET /sucursales/{} - Obteniendo sucursal por ID", id);
        try {
            Sucursal sucursal = gestionSucursalesService.obtenerPorId(id);
            return ResponseEntity.ok(sucursal);
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al obtener sucursal por ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener la sucursal");
        }
    }

    @PostMapping
    public ResponseEntity<Sucursal> crearSucursal(@RequestBody CreateSucursalDto dto) {
        logger.info("POST /sucursales - Creando sucursal: {}", dto.getNombre());
        try {
            Sucursal sucursal = gestionSucursalesService.crearSucursal(
                    dto.getNombre(),
                    dto.getLatitud(),
                    dto.getLongitud(),
                    dto.getEsPrincipal()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(sucursal);
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al crear sucursal", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la sucursal");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizarSucursal(@PathVariable String id, @RequestBody CreateSucursalDto dto) {
        logger.info("PUT /sucursales/{} - Actualizando sucursal: {}", id, dto.getNombre());
        try {
            Sucursal sucursal = gestionSucursalesService.actualizarSucursal(
                    id,
                    dto.getNombre(),
                    dto.getLatitud(),
                    dto.getLongitud(),
                    dto.getEsPrincipal()
            );
            return ResponseEntity.ok(sucursal);
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al actualizar sucursal: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar la sucursal");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarSucursal(@PathVariable String id) {
        logger.info("DELETE /sucursales/{} - Eliminando sucursal", id);
        try {
            gestionSucursalesService.eliminarSucursal(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Sucursal eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al eliminar sucursal: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la sucursal");
        }
    }
}
