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

    /**
     * Nuevo Endpoint para la Lógica de Salida.
     * Este método simula la validación que haría el vendedor al recibir la carga.
     * Cambia el estado de "PENDIENTE" a "VALIDADO".
     */
    @PatchMapping("/confirmar-salida/{idCarga}")
    public ResponseEntity<Map<String, Object>> confirmarSalida(@PathVariable String idCarga) {
        try {
            // Llamamos a la lógica de negocio para validar la recepción física
            AsignacionStock validada = gestionInventarioService.confirmarSalidaInventario(idCarga);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id_carga", idCarga);
            response.put("estado_final", "VALIDADO");
            response.put("mensaje", "Salida de inventario confirmada. El stock ya está en el transporte ");
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}