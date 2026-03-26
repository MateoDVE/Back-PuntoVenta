package com.back.puntoventa.app.domain.vendedores.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import com.back.puntoventa.app.domain.vendedores.port.GestorUsuariosPort;
import com.back.puntoventa.app.domain.vendedores.port.VendedorRepositoryPort;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso: Gestión de vendedores.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionVendedoresService {

    private final VendedorRepositoryPort vendedorRepositoryPort;
    private final GestorUsuariosPort gestorUsuariosPort;

    public GestionVendedoresService(VendedorRepositoryPort vendedorRepositoryPort, GestorUsuariosPort gestorUsuariosPort) {
        this.vendedorRepositoryPort = vendedorRepositoryPort;
        this.gestorUsuariosPort = gestorUsuariosPort;
    }

    public Vendedor crearVendedor(String nombre, String email, String password) {
        nombre = nombre.trim();
        email = email.trim().toLowerCase();

        if (nombre.isBlank()) {
            throw new DomainException("Nombre no puede estar vacío");
        }
        if (email.isBlank()) {
            throw new DomainException("Email no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            throw new DomainException("Contraseña no puede estar vacía");
        }

        if (vendedorRepositoryPort.existeEmail(email, null)) {
            throw new DomainException("Ya existe un usuario con ese email");
        }

        Map<String, Object> authUser = gestorUsuariosPort.crearUsuario(email, password, nombre);
        Object createdId = authUser.get("id");
        if (!(createdId instanceof String userId) || userId.isBlank()) {
            throw new DomainException("No se pudo crear el usuario en autenticación");
        }

        Vendedor vendedor = new Vendedor(userId, nombre, email, "ACTIVO", LocalDateTime.now());
        return vendedorRepositoryPort.crear(vendedor);
    }

    public List<Vendedor> obtenerTodos() {
        return vendedorRepositoryPort.obtenerTodos();
    }

    public Vendedor obtenerPorId(String id) {
        return vendedorRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Vendedor no encontrado"));
    }

    public Vendedor actualizar(String id, String nombre, String email, String password, String estado) {
        Vendedor vendedorActual = obtenerPorId(id);

        Map<String, Object> authUpdates = new HashMap<>();
        String nombreFinal = vendedorActual.getNombre();
        String emailFinal = vendedorActual.getEmail();
        String estadoFinal = vendedorActual.getEstado();

        if (nombre != null && !nombre.isBlank()) {
            nombreFinal = nombre.trim();
            authUpdates.put("user_metadata", Map.of("nombre", nombreFinal));
        }

        if (email != null && !email.isBlank()) {
            emailFinal = email.trim().toLowerCase();
            if (!emailFinal.equals(vendedorActual.getEmail()) &&
                    vendedorRepositoryPort.existeEmail(emailFinal, id)) {
                throw new DomainException("Ya existe un usuario con ese email");
            }
            authUpdates.put("email", emailFinal);
        }

        if (password != null && !password.isBlank()) {
            if (password.length() < 4) {
                throw new DomainException("La contraseña debe tener al menos 4 caracteres");
            }
            authUpdates.put("password", password);
        }

        if (estado != null && !estado.isBlank()) {
            estadoFinal = estado.trim().toUpperCase();
        }

        if (!authUpdates.isEmpty()) {
            gestorUsuariosPort.actualizarUsuario(id, authUpdates);
        }

        Vendedor vendedorActualizado =
                new Vendedor(id, nombreFinal, emailFinal, estadoFinal, vendedorActual.getCreatedAt());
        return vendedorRepositoryPort.actualizar(id, vendedorActualizado);
    }

    public void eliminar(String id) {
        obtenerPorId(id);
        gestorUsuariosPort.eliminarUsuario(id);
        vendedorRepositoryPort.eliminar(id);
    }
}
