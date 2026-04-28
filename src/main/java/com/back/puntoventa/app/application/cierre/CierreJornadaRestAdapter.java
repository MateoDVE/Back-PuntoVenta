package com.back.puntoventa.app.application.cierre;

import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.domain.cierre.model.CierreJornada;
import com.back.puntoventa.app.domain.cierre.model.request.RegistrarCierreRequest;
import com.back.puntoventa.app.domain.cierre.service.GestionCierreJornadaService;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador primario (HTTP): expone los casos de uso de cierres de jornada.
 */
@RestController
@RequestMapping("/cierres")
@CrossOrigin(origins = "http://localhost:4200")
public class CierreJornadaRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CierreJornadaRestAdapter.class);

    private final GestionCierreJornadaService gestionCierreService;

    public CierreJornadaRestAdapter(GestionCierreJornadaService gestionCierreService) {
        this.gestionCierreService = gestionCierreService;
    }

    /** POST /cierres — Registrar un nuevo cierre de jornada */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody RegistrarCierreRequest request) {
        logger.info("POST /cierres - Registrar cierre para vendedor {} en fecha {}", request.getIdVendedor(), request.getFecha());
        try {
            CierreJornada cierre = gestionCierreService.registrarCierre(request);
            logger.info("Cierre registrado con ID: {}", cierre.getIdCierre());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cierre));
        } catch (DomainException ex) {
            logger.warn("Error de dominio al registrar cierre: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al registrar cierre: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al registrar cierre", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** GET /cierres — Listar todos los cierres */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarTodos() {
        logger.info("GET /cierres - Listar todos los cierres");
        try {
            List<Map<String, Object>> response = gestionCierreService.obtenerTodos()
                    .stream().map(this::toResponse).toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al listar cierres", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** GET /cierres/{id} — Obtener cierre por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        logger.info("GET /cierres/{} - Obtener cierre por ID", id);
        try {
            return ResponseEntity.ok(toResponse(gestionCierreService.obtenerPorId(id)));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al obtener cierre por ID", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** GET /cierres/vendedor/{idVendedor} — Historial de cierres del vendedor */
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorVendedor(@PathVariable UUID idVendedor) {
        logger.info("GET /cierres/vendedor/{} - Historial de cierres", idVendedor);
        try {
            List<Map<String, Object>> response = gestionCierreService.obtenerPorVendedor(idVendedor)
                    .stream().map(this::toResponse).toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al obtener cierres del vendedor", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** GET /cierres/vendedor/{idVendedor}/fecha/{fecha} — Cierre de una fecha específica */
    @GetMapping("/vendedor/{idVendedor}/fecha/{fecha}")
    public ResponseEntity<Map<String, Object>> obtenerPorVendedorYFecha(
            @PathVariable UUID idVendedor,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("GET /cierres/vendedor/{}/fecha/{} - Obtener cierre específico", idVendedor, fecha);
        try {
            return ResponseEntity.ok(toResponse(gestionCierreService.obtenerPorVendedorYFecha(idVendedor, fecha)));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al obtener cierre por vendedor y fecha", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** PUT /cierres/{id} — Actualizar un cierre */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String id,
            @RequestBody RegistrarCierreRequest request) {
        logger.info("PUT /cierres/{} - Actualizar cierre", id);
        try {
            CierreJornada actualizado = gestionCierreService.actualizarCierre(id, request);
            return ResponseEntity.ok(toResponse(actualizado));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al actualizar cierre", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /** DELETE /cierres/{id} — Eliminar un cierre */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        logger.info("DELETE /cierres/{} - Eliminar cierre", id);
        try {
            gestionCierreService.eliminarCierre(id);
            return ResponseEntity.noContent().build();
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al eliminar cierre", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Map<String, Object> toResponse(CierreJornada c) {
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("id_cierre", c.getIdCierre());
        r.put("id_vendedor", c.getIdVendedor() != null ? c.getIdVendedor().toString() : null);
        r.put("fecha", c.getFecha() != null ? c.getFecha().toString() : null);
        r.put("ventas_realizadas", c.getVentasRealizadas());
        r.put("total_efectivo", c.getTotalEfectivo());
        r.put("total_descuentos", c.getTotalDescuentos());
        r.put("stock_inicial_total", c.getStockInicialTotal());
        r.put("vendidos_total", c.getVendidosTotal());
        r.put("stock_final_total", c.getStockFinalTotal());
        r.put("estado_inventario", c.getEstadoInventario());
        r.put("dinero_esperado", c.getDineroEsperado());
        r.put("dinero_contado", c.getDineroContado());
        r.put("diferencia", c.getDiferencia());
        r.put("estado_efectivo", c.getEstadoEfectivo());
        r.put("estado", c.getEstado());
        r.put("created_at", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        return r;
    }
}
