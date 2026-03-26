package com.back.puntoventa.app.application.vendedores.controller;

import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import com.back.puntoventa.app.domain.vendedores.service.GestionVendedoresService;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.application.vendedores.dto.CreateVendedorDto;
import com.back.puntoventa.app.application.vendedores.dto.UpdateVendedorDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de
 * vendedores
 * del dominio al mundo exterior.
 */
@RestController
@RequestMapping("/vendedores")
@CrossOrigin(origins = "http://localhost:4200")
public class VendedoresRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(VendedoresRestAdapter.class);
    private final GestionVendedoresService gestionVendedoresService;

    public VendedoresRestAdapter(GestionVendedoresService gestionVendedoresService) {
        this.gestionVendedoresService = gestionVendedoresService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody CreateVendedorDto request) {
        logger.info("POST /vendedores - Crear nuevo vendedor: {}", request.getEmail());
        try {
            Vendedor vendedor = gestionVendedoresService.crearVendedor(
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword());
            logger.info("Vendedor creado exitosamente - ID: {}, Email: {}", vendedor.getId(), vendedor.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            logger.warn("Error al crear vendedor - Email: {}, Motivo: {}", request.getEmail(), ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al crear vendedor - Email: {}", request.getEmail(), ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        logger.info("GET /vendedores - Obtener todos los vendedores");
        try {
            List<Vendedor> vendedores = gestionVendedoresService.obtenerTodos();
            logger.debug("Se obtuvieron {} vendedores", vendedores.size());
            List<Map<String, Object>> response = vendedores.stream()
                    .map(this::mapVendedorToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al obtener vendedores", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        logger.info("GET /vendedores/{} - Obtener vendedor por ID", id);
        try {
            Vendedor vendedor = gestionVendedoresService.obtenerPorId(id);
            logger.debug("Vendedor encontrado - ID: {}, Email: {}", id, vendedor.getEmail());
            return ResponseEntity.ok(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            logger.warn("Vendedor no encontrado - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String id,
            @RequestBody UpdateVendedorDto request) {
        logger.info("PUT /vendedores/{} - Actualizar vendedor", id);
        try {
            Vendedor vendedor = gestionVendedoresService.actualizar(
                    id,
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getEstado());
            logger.info("Vendedor actualizado exitosamente - ID: {}", id);
            return ResponseEntity.ok(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            logger.warn("Error al actualizar vendedor - ID: {}, Motivo: {}", id, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al actualizar vendedor - ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        logger.info("DELETE /vendedores/{} - Eliminar vendedor", id);
        try {
            gestionVendedoresService.eliminar(id);
            logger.info("Vendedor eliminado exitosamente - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            logger.warn("Vendedor no encontrado para eliminar - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al eliminar vendedor - ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Map<String, Object> mapVendedorToResponse(Vendedor vendedor) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id_usuario", vendedor.getId());
        response.put("nombre", vendedor.getNombre());
        response.put("email", vendedor.getEmail());
        response.put("estado", vendedor.getEstado());
        if (vendedor.getCreatedAt() != null) {
            response.put("created_at", vendedor.getCreatedAt().toString());
        }
        return response;
    }
}
