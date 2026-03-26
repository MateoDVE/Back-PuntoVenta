package com.back.puntoventa.app.application.auth.controller;

import com.back.puntoventa.app.domain.auth.model.Usuario;
import com.back.puntoventa.app.domain.auth.service.AutorizacionAuthService;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.application.auth.dto.SignInDto;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de autenticación
 * del dominio al mundo exterior.
 *
 * Mapea solicitudes HTTP a llamadas de dominio y convierte respuestas de dominio
 * a dtos HTTP.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestAdapter {

    private final AutorizacionAuthService autorizacionService;

    public AuthRestAdapter(AutorizacionAuthService autorizacionService) {
        this.autorizacionService = autorizacionService;
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody SignInDto request) {
        try {
            Map<String, Object> response = autorizacionService.autenticar(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            Usuario usuario = autorizacionService.obtenerPerfilPorToken(token);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id_usuario", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol());
            response.put("estado", usuario.getEstado());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam String userId) {
        try {
            Usuario usuario = autorizacionService.obtenerPerfil(userId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id_usuario", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol());
            response.put("estado", usuario.getEstado());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(@RequestParam String userId) {
        autorizacionService.cerrarSesion(userId);
        return ResponseEntity.noContent().build();
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token inválido o faltante");
        }
        return authHeader.substring(7);
    }
}
