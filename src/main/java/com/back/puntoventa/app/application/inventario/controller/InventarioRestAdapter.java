package com.back.puntoventa.app.application.inventario.controller;

import com.back.puntoventa.app.application.inventario.dto.AsignarStockDto;
import com.back.puntoventa.app.application.inventario.dto.CargaInicialStockDto;
import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.service.AutorizacionAuthService;
import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.service.GestionInventarioService;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventario")
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InventarioRestAdapter.class);

    private final GestionInventarioService gestionInventarioService;
    private final AutorizacionAuthService autorizacionAuthService;

    public InventarioRestAdapter(
            GestionInventarioService gestionInventarioService,
            AutorizacionAuthService autorizacionAuthService) {
        this.gestionInventarioService = gestionInventarioService;
        this.autorizacionAuthService = autorizacionAuthService;
    }

    @PostMapping("/asignar")
    public ResponseEntity<Map<String, Object>> asignarStock(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AsignarStockDto request) {
        try {
            Usuario actor = getActorDesdeToken(authHeader);
            validarRol(actor, "ADMIN", "ADMINISTRADOR");
            logger.info("POST /inventario/asignar - admin {} asigna stock", actor.getEmail());

            AsignacionStock asignacion = gestionInventarioService.asignarStockAVendedor(
                    request.getIdProducto(),
                    request.getIdVendedor(),
                    request.getCantidad());

            Map<String, Object> response = new HashMap<>();
            response.put("id_carga", asignacion.getIdCarga());
            response.put("id_vendedor", asignacion.getIdVendedor());
            response.put("id_producto", asignacion.getIdProducto());
            response.put("cantidad_inicial", asignacion.getCantidadAsignada());
            response.put("cantidad_actual", asignacion.getCantidadActual());
            response.put("estado_validacion", asignacion.getEstadoValidacion());
            response.put("fecha_asignacion", asignacion.getFechaAsignacion());
            response.put("mensaje", "Carga de transporte iniciada correctamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error en /inventario/asignar: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/stock-inicial")
    public ResponseEntity<Map<String, Object>> cargarStockInicial(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CargaInicialStockDto request) {
        try {
            Usuario actor = getActorDesdeToken(authHeader);
            validarRol(actor, "ADMIN", "ADMINISTRADOR");
            logger.info("POST /inventario/stock-inicial - admin {} carga stock inicial", actor.getEmail());

            Producto actualizado = gestionInventarioService.cargarStockInicialAlmacen(
                    request.getIdProducto(),
                    request.getCantidad());

            Map<String, Object> response = new HashMap<>();
            response.put("id_producto", actualizado.getId());
            response.put("stock_almacen_central", actualizado.getStockAlmacenCentral());
            response.put("mensaje", "Stock inicial cargado correctamente");
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error en /inventario/stock-inicial: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<Map<String, Object>>> obtenerCargasPorVendedor(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idVendedor) {
        try {
            Usuario actor = getActorDesdeToken(authHeader);
            String rol = normalizarRol(actor.getRol());

            boolean esAdmin = "ADMIN".equals(rol) || "ADMINISTRADOR".equals(rol);
            boolean esMismoVendedor = actor.getId().equals(idVendedor);

            if (!esAdmin && !esMismoVendedor) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "No tienes permisos para consultar el inventario de otro vendedor");
            }

            logger.info("GET /inventario/vendedor/{} - consulta por {}", idVendedor, actor.getEmail());
            List<AsignacionStock> cargas = gestionInventarioService.obtenerCargasPorVendedor(idVendedor);

            List<Map<String, Object>> response = cargas.stream().map(this::mapAsignacion).toList();
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error en consulta de inventario vendedor {}: {}", idVendedor, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/validar-admin/{idCarga}")
    public ResponseEntity<Map<String, Object>> validarAdmin(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idCarga) {
        try {
            Usuario actor = getActorDesdeToken(authHeader);
            validarRol(actor, "ADMIN", "ADMINISTRADOR");
            logger.info("PATCH /inventario/validar-admin/{} por {}", idCarga, actor.getEmail());

            AsignacionStock validada = gestionInventarioService.confirmarValidacionAdmin(idCarga);
            Map<String, Object> response = mapAsignacion(validada);
            response.put("mensaje", "Validación de administrador aplicada");
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error en validar-admin para {}: {}", idCarga, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /**
     * Validación final de salida por vendedor.
     * Flujo esperado: PENDIENTE -> VALIDADO_ADMIN -> VALIDADO.
     */
    @PatchMapping("/confirmar-salida/{idCarga}")
    public ResponseEntity<Map<String, Object>> confirmarSalida(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idCarga) {
        try {
            Usuario actor = getActorDesdeToken(authHeader);
            validarRol(actor, "VENDEDOR");
            logger.info("PATCH /inventario/confirmar-salida/{} por vendedor {}", idCarga, actor.getEmail());

            AsignacionStock validada = gestionInventarioService.confirmarSalidaInventario(idCarga, actor.getId());

            Map<String, Object> response = mapAsignacion(validada);
            response.put("mensaje", "Salida de inventario confirmada. El stock ya está en el transporte ");

            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("Error en confirmar-salida para {}: {}", idCarga, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private Map<String, Object> mapAsignacion(AsignacionStock asignacion) {
        Map<String, Object> response = new HashMap<>();
        response.put("id_carga", asignacion.getIdCarga());
        response.put("id_vendedor", asignacion.getIdVendedor());
        response.put("id_producto", asignacion.getIdProducto());
        response.put("cantidad_inicial", asignacion.getCantidadAsignada());
        response.put("cantidad_actual", asignacion.getCantidadActual());
        response.put("estado_validacion", asignacion.getEstadoValidacion());
        response.put("fecha_asignacion", asignacion.getFechaAsignacion());
        return response;
    }

    private Usuario getActorDesdeToken(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        return autorizacionAuthService.obtenerPerfilPorToken(token);
    }

    private void validarRol(Usuario actor, String... rolesPermitidos) {
        String rolActor = normalizarRol(actor.getRol());
        for (String rolPermitido : rolesPermitidos) {
            if (normalizarRol(rolPermitido).equals(rolActor)) {
                return;
            }
        }

        throw new ApiException(HttpStatus.FORBIDDEN,
                "No tienes permisos para ejecutar esta operación. Rol actual: " + actor.getRol());
    }

    private String normalizarRol(String rol) {
        return rol == null ? "" : rol.trim().toUpperCase();
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o faltante");
        }
        return authHeader.substring(7);
    }
}