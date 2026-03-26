package com.back.puntoventa.app.application.auth.controller;

import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.service.AutorizacionAuthService;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.application.auth.dto.SignInDto;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de autenticación
 * del dominio al mundo exterior.
 *
 * Mapea solicitudes HTTP a llamadas de dominio y convierte respuestas de
 * dominio
 * a dtos HTTP.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestAdapter.class);
    private final AutorizacionAuthService autorizacionService;

    public AuthRestAdapter(AutorizacionAuthService autorizacionService) {
        this.autorizacionService = autorizacionService;
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody SignInDto request) {
        logger.info("POST /auth/signin - Intento de autenticación para email: {}", request.getEmail());
        try {
            Map<String, Object> response = autorizacionService.autenticar(request.getEmail(), request.getPassword());
            logger.info("Autenticación exitosa para email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Fallo en autenticación para email: {} - Motivo: {}", request.getEmail(), ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@RequestHeader("Authorization") String authHeader) {
        logger.info("GET /auth/me - Obtener perfil del usuario actual");
        try {
            String token = extractTokenFromHeader(authHeader);
            Usuario usuario = autorizacionService.obtenerPerfilPorToken(token);
            logger.debug("Perfil obtenido para usuario: {}", usuario.getEmail());
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id_usuario", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol());
            response.put("estado", usuario.getEstado());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Error al obtener perfil: {}", ex.getMessage());
            throw new ApiException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam String userId) {
        logger.info("GET /auth/profile - Obtener perfil para usuario: {}", userId);
        try {
            Usuario usuario = autorizacionService.obtenerPerfil(userId);
            logger.debug("Perfil obtenido para usuario ID: {}", userId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id_usuario", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol());
            response.put("estado", usuario.getEstado());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Usuario no encontrado: {}", userId);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(@RequestParam String userId) {
        logger.info("POST /auth/signout - Cerrar sesión para usuario: {}", userId);
        autorizacionService.cerrarSesion(userId);
        logger.info("Sesión cerrada exitosamente para usuario: {}", userId);
        return ResponseEntity.noContent().build();
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token inválido o faltante");
        }
        return authHeader.substring(7);
    }
}
