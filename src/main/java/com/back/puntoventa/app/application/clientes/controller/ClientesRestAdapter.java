package com.back.puntoventa.app.application.clientes.controller;

import com.back.puntoventa.app.application.clientes.dto.CreateClienteDto;
import com.back.puntoventa.app.application.clientes.dto.UpdateClienteDto;
import com.back.puntoventa.app.domain.clientes.model.Cliente;
import com.back.puntoventa.app.domain.clientes.service.GestionClientesService;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientesRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientesRestAdapter.class);
    private final GestionClientesService gestionClientesService;
    private final SupabaseHttpClient supabaseHttpClient;

    public ClientesRestAdapter(GestionClientesService gestionClientesService,
            SupabaseHttpClient supabaseHttpClient) {
        this.gestionClientesService = gestionClientesService;
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerClientes(@RequestParam(required = false) String vendedorId) {
        logger.debug("Obteniendo clientes; vendedorId={}", vendedorId);
        if (vendedorId != null && !vendedorId.isBlank()) {
            return ResponseEntity.ok(gestionClientesService.obtenerPorVendedor(vendedorId));
        }
        return ResponseEntity.ok(gestionClientesService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable String id) {
        logger.debug("Obteniendo cliente por id={}", id);
        return ResponseEntity.ok(gestionClientesService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody CreateClienteDto dto) {
        logger.info("Creando cliente; negocio={}, ciNit={}", dto.getNombreNegocio(), dto.getCiNit());
        Cliente cliente = gestionClientesService.crearCliente(
                dto.getIdVendedorCreador(),
                dto.getNombreNegocio(),
                dto.getCiNit(),
                dto.getCelular(),
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getUrlFotoFachada(),
                dto.getFrecuenciaVisita());
        return ResponseEntity.ok(cliente);
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, Object>> uploadFotoCliente(@RequestParam("image") MultipartFile file) {
        logger.info("POST /clientes/upload-photo - Subiendo foto de cliente: {}", file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                logger.warn("Archivo de cliente vacío");
                throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isBlank()) {
                fileName = "cliente_" + System.currentTimeMillis() + ".jpg";
            }
            String mimeType = file.getContentType();
            byte[] content = file.getBytes();

            if (mimeType == null || !mimeType.toLowerCase().startsWith("image/")) {
                logger.warn("Tipo MIME inválido para foto de cliente: {}", mimeType);
                throw new ApiException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen");
            }

            String path = "temp/" + fileName;
            String url = supabaseHttpClient.uploadToStorage("clientes", path, content, mimeType);
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", url);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error al subir foto de cliente", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir la foto: " + ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable String id,
            @RequestBody UpdateClienteDto dto) {
        logger.info("Actualizando cliente id={}; negocio={}, ciNit={}", id, dto.getNombreNegocio(), dto.getCiNit());
        Cliente clienteActualizado = gestionClientesService.actualizarCliente(
                id,
                dto.getNombreNegocio(),
                dto.getCiNit(),
                dto.getCelular(),
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getUrlFotoFachada(),
                dto.getFrecuenciaVisita(),
                dto.getEstado());
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable String id) {
        logger.info("Eliminando cliente id={}", id);
        gestionClientesService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
