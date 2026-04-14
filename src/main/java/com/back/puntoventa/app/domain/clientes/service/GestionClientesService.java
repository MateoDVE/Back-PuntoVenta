package com.back.puntoventa.app.domain.clientes.service;

import com.back.puntoventa.app.domain.clientes.model.Cliente;
import com.back.puntoventa.app.domain.clientes.port.ClienteRepositoryPort;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GestionClientesService {

    private static final Logger logger = LoggerFactory.getLogger(GestionClientesService.class);
    private final ClienteRepositoryPort clienteRepositoryPort;

    public GestionClientesService(ClienteRepositoryPort clienteRepositoryPort) {
        this.clienteRepositoryPort = clienteRepositoryPort;
    }

    public Cliente crearCliente(
            String idVendedorCreador,
            String nombreNegocio,
            String ciNit,
            String celular,
            Double latitud,
            Double longitud,
            String urlFotoFachada,
            String frecuenciaVisita) {
        logger.debug("Creando cliente - Negocio: {}, CI/NIT: {}", nombreNegocio, ciNit);

        if (idVendedorCreador == null || idVendedorCreador.isBlank()) {
            throw new DomainException("El identificador del vendedor creador es obligatorio");
        }
        if (nombreNegocio == null || nombreNegocio.isBlank()) {
            throw new DomainException("El nombre del negocio es obligatorio");
        }
        if (ciNit == null || ciNit.isBlank()) {
            throw new DomainException("El CI/NIT es obligatorio");
        }
        String ciNitFinal = ciNit.trim();
        if (clienteRepositoryPort.existeCiNit(ciNitFinal, null)) {
            throw new DomainException("Ya existe un cliente con ese CI/NIT");
        }

        String celularFinal = null;
        if (celular != null && !celular.isBlank()) {
            celularFinal = celular.trim();
            if (!celularFinal.matches("^[67]\\d{7}$")) {
                throw new DomainException("El celular debe tener 8 dígitos y empezar por 6 o 7");
            }
        }

        Cliente cliente = new Cliente(
                null,
                idVendedorCreador.trim(),
                nombreNegocio.trim(),
                ciNitFinal,
                celularFinal,
                latitud,
                longitud,
                urlFotoFachada == null ? null : urlFotoFachada.trim(),
                frecuenciaVisita == null ? null : frecuenciaVisita.trim(),
                "ACTIVO",
                LocalDateTime.now());

        Cliente creado = clienteRepositoryPort.crear(cliente);
        logger.info("Cliente creado exitosamente - ID: {}", creado.getId());
        return creado;
    }

    public List<Cliente> obtenerTodos() {
        logger.debug("Obteniendo todos los clientes");
        return clienteRepositoryPort.obtenerTodos();
    }

    public Cliente obtenerPorId(String id) {
        logger.debug("Obteniendo cliente por ID: {}", id);
        return clienteRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Cliente no encontrado"));
    }

    public List<Cliente> obtenerPorVendedor(String idVendedor) {
        logger.debug("Obteniendo clientes por vendedor: {}", idVendedor);
        return clienteRepositoryPort.obtenerPorVendedor(idVendedor);
    }

    public Cliente actualizarCliente(
            String id,
            String nombreNegocio,
            String ciNit,
            String celular,
            Double latitud,
            Double longitud,
            String urlFotoFachada,
            String frecuenciaVisita,
            String estado) {
        logger.debug("Actualizando cliente - ID: {}", id);

        Cliente clienteActual = obtenerPorId(id);

        String nombreNegocioFinal = clienteActual.getNombreNegocio();
        String ciNitFinal = clienteActual.getCiNit();
        String celularFinal = clienteActual.getCelular();
        Double latitudFinal = clienteActual.getLatitud();
        Double longitudFinal = clienteActual.getLongitud();
        String urlFotoFachadaFinal = clienteActual.getUrlFotoFachada();
        String frecuenciaVisitaFinal = clienteActual.getFrecuenciaVisita();
        String estadoFinal = clienteActual.getEstado();

        if (nombreNegocio != null && !nombreNegocio.isBlank()) {
            nombreNegocioFinal = nombreNegocio.trim();
        }
        if (ciNit != null && !ciNit.isBlank()) {
            ciNitFinal = ciNit.trim();
            if (!ciNitFinal.equals(clienteActual.getCiNit()) && clienteRepositoryPort.existeCiNit(ciNitFinal, id)) {
                throw new DomainException("Ya existe un cliente con ese CI/NIT");
            }
        }
        if (celular != null) {
            if (!celular.isBlank()) {
                celularFinal = celular.trim();
                if (!celularFinal.matches("^[67]\\d{7}$")) {
                    throw new DomainException("El celular debe tener 8 dígitos y empezar por 6 o 7");
                }
            } else {
                celularFinal = null;
            }
        }
        if (latitud != null) {
            latitudFinal = latitud;
        }
        if (longitud != null) {
            longitudFinal = longitud;
        }
        if (urlFotoFachada != null) {
            urlFotoFachadaFinal = urlFotoFachada.trim();
        }
        if (frecuenciaVisita != null) {
            frecuenciaVisitaFinal = frecuenciaVisita.trim();
        }
        if (estado != null && !estado.isBlank()) {
            estadoFinal = estado.trim().toUpperCase();
        }

        Cliente clienteActualizado = new Cliente(
                id,
                clienteActual.getIdVendedorCreador(),
                nombreNegocioFinal,
                ciNitFinal,
                celularFinal,
                latitudFinal,
                longitudFinal,
                urlFotoFachadaFinal,
                frecuenciaVisitaFinal,
                estadoFinal,
                clienteActual.getCreatedAt());

        Cliente actualizado = clienteRepositoryPort.actualizar(id, clienteActualizado);
        logger.info("Cliente actualizado exitosamente - ID: {}", id);
        return actualizado;
    }

    public void eliminarCliente(String id) {
        logger.debug("Eliminando cliente - ID: {}", id);
        obtenerPorId(id);
        clienteRepositoryPort.eliminar(id);
        logger.info("Cliente eliminado exitosamente - ID: {}", id);
    }
}
