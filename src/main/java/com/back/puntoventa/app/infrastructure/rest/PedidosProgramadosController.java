package com.back.puntoventa.app.infrastructure.rest;

import com.back.puntoventa.app.application.pedidos.PedidoProgramadoService;
import com.back.puntoventa.app.application.pedidos.dto.CreatePedidoProgramadoDto;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.pedidos.model.PedidoProgramado;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone el API para la gestión de pedidos programados (Preventa).
 */
@RestController
@RequestMapping("/api/pedidos-programados")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidosProgramadosController {

    private static final Logger logger = LoggerFactory.getLogger(PedidosProgramadosController.class);

    private final PedidoProgramadoService pedidoProgramadoService;

    public PedidosProgramadosController(PedidoProgramadoService pedidoProgramadoService) {
        this.pedidoProgramadoService = pedidoProgramadoService;
    }

    /**
     * Endpoint POST para registrar un nuevo pedido programado y reservar stock.
     * 
     * @param request DTO de entrada validado.
     * @return Respuesta estructurada JSON con status, mensaje y el objeto del pedido creado.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearPedidoProgramado(@Valid @RequestBody CreatePedidoProgramadoDto request) {
        logger.info("POST /api/pedidos-programados - Crear nuevo pedido programado para cliente: {}", request.getIdCliente());
        try {
            PedidoProgramado pedido = pedidoProgramadoService.crearPedidoProgramado(request);
            logger.info("Pedido programado registrado exitosamente - ID: {}", pedido.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "Pedido programado registrado con éxito");
            response.put("data", pedido);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DomainException ex) {
            logger.warn("Error de validación/dominio al registrar pedido programado: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al registrar pedido programado: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error no controlado al procesar pedido programado", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
