package com.back.puntoventa.app.infrastructure.rest;

import com.back.puntoventa.app.domain.ruta.model.Ubicacion;
import com.back.puntoventa.app.domain.ruta.service.RutaService;
import com.back.puntoventa.app.infrastructure.rest.dto.UbicacionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ruta")
@CrossOrigin(origins = "*")
public class RutaRestAdapter {

    private final RutaService rutaService;

    public RutaRestAdapter(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    // Endpoint para que el vendedor mande su GPS
    @PostMapping("/actualizar-ubicacion")
    public ResponseEntity<String> actualizarUbicacion(@RequestBody UbicacionRequest request) {
        Ubicacion ubicacion = new Ubicacion(
            request.getIdVendedor(),
            request.getLatitud(),
            request.getLongitud()
        );
        rutaService.registrarSeguimiento(ubicacion);
        return ResponseEntity.ok("Ubicación registrada exitosamente");
    }

    // Endpoint para que el administrador vea el historial
    @GetMapping("/historial/{idVendedor}")
    public ResponseEntity<List<Ubicacion>> obtenerHistorial(@PathVariable String idVendedor) {
        return ResponseEntity.ok(rutaService.obtenerHistorial(idVendedor));
    }
}