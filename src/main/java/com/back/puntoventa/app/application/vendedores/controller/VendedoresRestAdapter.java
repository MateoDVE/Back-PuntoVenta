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

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de vendedores
 * del dominio al mundo exterior.
 */
@RestController
@RequestMapping("/vendedores")
@CrossOrigin(origins = "http://localhost:4200")
public class VendedoresRestAdapter {

    private final GestionVendedoresService gestionVendedoresService;

    public VendedoresRestAdapter(GestionVendedoresService gestionVendedoresService) {
        this.gestionVendedoresService = gestionVendedoresService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody CreateVendedorDto request) {
        try {
            Vendedor vendedor = gestionVendedoresService.crearVendedor(
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            List<Vendedor> vendedores = gestionVendedoresService.obtenerTodos();
            List<Map<String, Object>> response = vendedores.stream()
                    .map(this::mapVendedorToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            Vendedor vendedor = gestionVendedoresService.obtenerPorId(id);
            return ResponseEntity.ok(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String id,
            @RequestBody UpdateVendedorDto request) {
        try {
            Vendedor vendedor = gestionVendedoresService.actualizar(
                    id,
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getEstado()
            );
            return ResponseEntity.ok(mapVendedorToResponse(vendedor));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        try {
            gestionVendedoresService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
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
