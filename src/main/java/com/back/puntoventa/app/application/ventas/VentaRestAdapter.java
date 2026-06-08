package com.back.puntoventa.app.application.ventas;

import com.back.puntoventa.app.domain.ventas.model.request.ConfirmarCierreRequest;
import com.back.puntoventa.app.domain.ventas.model.request.CrearVentaRequest;
import com.back.puntoventa.app.domain.ventas.model.response.ConfirmarCierreResponse;
import com.back.puntoventa.app.domain.ventas.model.response.CierreJornadaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ResumenDiarioResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResumenResponse;
import com.back.puntoventa.app.domain.ventas.model.response.VentaResponse;
import com.back.puntoventa.app.domain.ventas.model.response.ReportesResumenResponse;
import com.back.puntoventa.app.domain.ventas.service.GestionVentasService;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.common.ApiException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping("/sincronizar")
    public ResponseEntity<List<VentaResponse>> sincronizar(@RequestBody List<CrearVentaRequest> ventas) {
        logger.info("Sincronizando lote de {} ventas", ventas.size());
        
        // Gracias a la idempotencia que programaste, si alguna ya existe, no se duplicará
        List<VentaResponse> respuestas = ventas.stream()
                .map(gestionVentasService::crearVenta)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(respuestas);
    }
    @GetMapping
    public ResponseEntity<List<VentaResumenResponse>> obtenerVentas() {
        logger.info("GET /ventas - Listar ventas resumidas");
        try {
            return ResponseEntity.ok(gestionVentasService.obtenerVentas());
        } catch (Exception ex) {
            logger.error("Error interno al listar ventas", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerVentaPorId(@PathVariable String id) {
        logger.info("GET /ventas/{} - Obtener venta por id", id);
        try {
            return ResponseEntity.ok(gestionVentasService.obtenerVentaPorId(id));
        } catch (DomainException ex) {
            logger.warn("Venta no encontrada o inválida: {}", ex.getMessage());
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al obtener venta: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al obtener venta", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/resumen-diario")
    public ResponseEntity<ResumenDiarioResponse> obtenerResumenDiario(
            @RequestParam UUID idVendedor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("GET /ventas/resumen-diario - Obtener resumen diario para vendedor {} y fecha {}", idVendedor, fecha);
        try {
            return ResponseEntity.ok(gestionVentasService.obtenerResumenDiario(idVendedor, fecha));
        } catch (DomainException ex) {
            logger.warn("Error de dominio al obtener resumen diario: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al obtener resumen diario: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al obtener resumen diario", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/cierre-jornada")
    public ResponseEntity<CierreJornadaResponse> obtenerResumenCierreJornada(
            @RequestParam UUID idVendedor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("GET /ventas/cierre-jornada - Obtener cierre de jornada para vendedor {} y fecha {}", idVendedor, fecha);
        try {
            return ResponseEntity.ok(gestionVentasService.obtenerResumenCierreJornada(idVendedor, fecha));
        } catch (DomainException ex) {
            logger.warn("Error de dominio al obtener cierre de jornada: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al obtener cierre de jornada: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al obtener cierre de jornada", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/confirmar-cierre")
    public ResponseEntity<ConfirmarCierreResponse> confirmarCierreJornada(@RequestBody ConfirmarCierreRequest request) {
        logger.info("POST /ventas/confirmar-cierre - Confirmar cierre de jornada para vendedor {} y fecha {}", request.idVendedor(), request.fecha());
        try {
            ConfirmarCierreResponse response = gestionVentasService.confirmarCierreJornada(request.idVendedor(), request.fecha(), request.dineroContado());
            logger.info("Cierre de jornada confirmado - Estado conciliación: {}", response.estadoConciliacion());
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al confirmar cierre: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al confirmar cierre: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al confirmar cierre", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/reportes")
    public ResponseEntity<ReportesResumenResponse> obtenerReportesConsolidados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        logger.info("GET /ventas/reportes - Obtener reportes consolidados para la fecha: {}", fechaConsulta);
        try {
            return ResponseEntity.ok(gestionVentasService.obtenerReportesConsolidados(fechaConsulta));
        } catch (Exception ex) {
            logger.error("Error interno al obtener reportes consolidados", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/devolver-stock")
    public ResponseEntity<java.util.Map<String, String>> devolverStock(@RequestBody java.util.Map<String, Object> payload) {
        String idVendedorStr = (String) payload.get("idVendedor");
        String fechaStr = (String) payload.get("fecha");
        
        if (idVendedorStr == null || idVendedorStr.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "idVendedor es obligatorio");
        }
        if (fechaStr == null || fechaStr.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fecha es obligatoria");
        }
        
        logger.info("POST /ventas/devolver-stock - Devolver stock al almacén para vendedor {} y fecha {}", idVendedorStr, fechaStr);
        try {
            UUID idVendedor = UUID.fromString(idVendedorStr);
            LocalDate fecha = LocalDate.parse(fechaStr);
            gestionVentasService.devolverStockAlmacen(idVendedor, fecha);
            
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Stock devuelto al almacén central exitosamente");
            return ResponseEntity.ok(response);
        } catch (DomainException ex) {
            logger.warn("Error de dominio al devolver stock: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al devolver stock: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al devolver stock", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}