package com.back.puntoventa.app.domain.vendedores.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.vendedores.model.Vendedor;
import com.back.puntoventa.app.domain.vendedores.port.GestorUsuariosPort;
import com.back.puntoventa.app.domain.vendedores.port.VendedorRepositoryPort;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: Gestión de vendedores.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionVendedoresService {

    private static final Logger logger = LoggerFactory.getLogger(GestionVendedoresService.class);
    private final VendedorRepositoryPort vendedorRepositoryPort;
    private final GestorUsuariosPort gestorUsuariosPort;

    public GestionVendedoresService(VendedorRepositoryPort vendedorRepositoryPort,
            GestorUsuariosPort gestorUsuariosPort) {
        this.vendedorRepositoryPort = vendedorRepositoryPort;
        this.gestorUsuariosPort = gestorUsuariosPort;
    }

    public Vendedor crearVendedor(String nombre, String email, String password) {
        logger.debug("Creando vendedor - Email: {}, Nombre: {}", email, nombre);
        nombre = nombre.trim();
        email = email.trim().toLowerCase();

        if (nombre.isBlank()) {
            logger.warn("Intento de crear vendedor con nombre vacío");
            throw new DomainException("Nombre no puede estar vacío");
        }
        if (email.isBlank()) {
            logger.warn("Intento de crear vendedor con email vacío");
            throw new DomainException("Email no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            logger.warn("Intento de crear vendedor sin contraseña");
            throw new DomainException("Contraseña no puede estar vacía");
        }

        if (vendedorRepositoryPort.existeEmail(email, null)) {
            logger.warn("Intento de crear vendedor con email existente: {}", email);
            throw new DomainException("Ya existe un usuario con ese email");
        }

        Map<String, Object> authUser = gestorUsuariosPort.crearUsuario(email, password, nombre);
        Object createdId = authUser.get("id");
        if (!(createdId instanceof String userId) || userId.isBlank()) {
            logger.error("No se pudo obtener ID de usuario en autenticación para email: {}", email);
            throw new DomainException("No se pudo crear el usuario en autenticación");
        }

        Vendedor vendedor = new Vendedor(userId, nombre, email, "ACTIVO", LocalDateTime.now());
        vendedor = vendedorRepositoryPort.crear(vendedor);
        logger.info("Vendedor creado exitosamente - ID: {}, Email: {}", vendedor.getId(), vendedor.getEmail());
        return vendedor;
    }

    public List<Vendedor> obtenerTodos() {
        logger.debug("Obteniendo todos los vendedores");
        List<Vendedor> vendedores = vendedorRepositoryPort.obtenerTodos();
        logger.debug("Se obtuvieron {} vendedores", vendedores.size());
        return vendedores;
    }

    public Vendedor obtenerPorId(String id) {
        logger.debug("Obteniendo vendedor por ID: {}", id);
        Vendedor vendedor = vendedorRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> {
                    logger.warn("Vendedor no encontrado - ID: {}", id);
                    return new DomainException("Vendedor no encontrado");
                });
        logger.debug("Vendedor encontrado - ID: {}, Email: {}", id, vendedor.getEmail());
        return vendedor;
    }

    public Vendedor actualizar(String id, String nombre, String email, String password, String estado) {
        logger.debug("Actualizando vendedor - ID: {}", id);
        Vendedor vendedorActual = obtenerPorId(id);

        Map<String, Object> authUpdates = new HashMap<>();
        String nombreFinal = vendedorActual.getNombre();
        String emailFinal = vendedorActual.getEmail();
        String estadoFinal = vendedorActual.getEstado();

        if (nombre != null && !nombre.isBlank()) {
            nombreFinal = nombre.trim();
            authUpdates.put("user_metadata", Map.of("nombre", nombreFinal));
            logger.debug("Nombre actualizado a: {}", nombreFinal);
        }

        if (email != null && !email.isBlank()) {
            emailFinal = email.trim().toLowerCase();
            if (!emailFinal.equals(vendedorActual.getEmail()) &&
                    vendedorRepositoryPort.existeEmail(emailFinal, id)) {
                logger.warn("Email existente en otro vendedor: {}", emailFinal);
                throw new DomainException("Ya existe un usuario con ese email");
            }
            authUpdates.put("email", emailFinal);
            logger.debug("Email actualizado a: {}", emailFinal);
        }

        if (password != null && !password.isBlank()) {
            if (password.length() < 4) {
                logger.warn("Contraseña muy corta para vendedor ID: {}", id);
                throw new DomainException("La contraseña debe tener al menos 4 caracteres");
            }
            authUpdates.put("password", password);
            logger.debug("Contraseña actualizada para vendedor ID: {}", id);
        }

        if (estado != null && !estado.isBlank()) {
            estadoFinal = estado.trim().toUpperCase();
            logger.debug("Estado actualizado a: {}", estadoFinal);
        }

        if (!authUpdates.isEmpty()) {
            gestorUsuariosPort.actualizarUsuario(id, authUpdates);
            logger.debug("Cambios aplicados al sistema de autenticación para vendedor ID: {}", id);
        }

        Vendedor vendedorActualizado = new Vendedor(id, nombreFinal, emailFinal, estadoFinal,
                vendedorActual.getCreatedAt());
        vendedorActualizado = vendedorRepositoryPort.actualizar(id, vendedorActualizado);
        logger.info("Vendedor actualizado exitosamente - ID: {}", id);
        return vendedorActualizado;
    }

    public void eliminar(String id) {
        logger.debug("Eliminando vendedor - ID: {}", id);
        obtenerPorId(id); // Valida que exista
        // gestorUsuariosPort.eliminarUsuario(id); // Comentado para permitir soft delete completo (evita borrar físicamente de Supabase Auth)
        vendedorRepositoryPort.eliminar(id);
        logger.info("Vendedor eliminado exitosamente - ID: {}", id);
    }
}
