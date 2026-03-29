package com.back.puntoventa.app.application.inventario.controller;

import com.back.puntoventa.app.application.inventario.dto.AsignarStockDto;
import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.service.GestionInventarioService;
import com.back.puntoventa.app.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/inventario")
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioRestAdapter {

    private final GestionInventarioService gestionInventarioService;

    public InventarioRestAdapter(GestionInventarioService gestionInventarioService) {
        this.gestionInventarioService = gestionInventarioService;
    }

    @PostMapping("/asignar")
    public ResponseEntity<Map<String, Object>> asignarStock(@RequestBody AsignarStockDto request) {
        try {
            AsignacionStock asignacion = gestionInventarioService.asignarStockAVendedor(
                    request.getIdProducto(),
                    request.getIdVendedor(),
                    request.getCantidad()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id_carga", asignacion.getIdCarga());
            response.put("vendedor", asignacion.getIdVendedor());
            response.put("estado", asignacion.getEstadoValidacion());
            response.put("mensaje", "Carga de transporte iniciada correctamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}