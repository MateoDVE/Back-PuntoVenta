package com.back.puntoventa.app.application.productos.controller;

import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.service.GestionProductosService;
import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.application.productos.dto.CreateProductoDto;
import com.back.puntoventa.app.application.productos.dto.UpdateProductoDto;
import com.back.puntoventa.app.application.productos.dto.DeleteImageDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de productos
 * del dominio al mundo exterior.
 */
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductosRestAdapter {

    private final GestionProductosService gestionProductosService;

    public ProductosRestAdapter(GestionProductosService gestionProductosService) {
        this.gestionProductosService = gestionProductosService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody CreateProductoDto request) {
        try {
            Producto producto = gestionProductosService.crearProducto(
                    request.getSku(),
                    request.getIdCategoria(),
                    request.getNombre(),
                    request.getDescripcion(),
                    request.getPrecioUnidad(),
                    request.getPrecioCaja(),
                    request.getUnidadesPorCaja(),
                    request.getStockAlmacenCentral()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            List<Producto> productos = gestionProductosService.obtenerTodos();
            List<Map<String, Object>> response = productos.stream()
                    .map(this::mapProductoToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            Producto producto = gestionProductosService.obtenerPorId(id);
            return ResponseEntity.ok(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String id,
            @RequestBody UpdateProductoDto request) {
        try {
            Producto producto = gestionProductosService.actualizar(
                    id,
                request.getSku(),
                request.getIdCategoria(),
                    request.getNombre(),
                    request.getDescripcion(),
                request.getUrlImagen(),
                request.getPrecioUnidad(),
                request.getPrecioCaja(),
                request.getUnidadesPorCaja(),
                request.getStockAlmacenCentral(),
                    request.getEstado()
            );
            return ResponseEntity.ok(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        try {
            gestionProductosService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (DomainException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> subirImagenSinId(
            @RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
            }

            String fileName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            byte[] content = file.getBytes();

            String url = gestionProductosService.subirImagenTemporal(fileName, content, mimeType);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("imageUrl", url);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir imagen: " + ex.getMessage());
        }
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<Map<String, Object>> subirImagen(
            @PathVariable String id,
            @RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
            }

            String fileName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            byte[] content = file.getBytes();

            String url = gestionProductosService.subirImagen(id, fileName, content, mimeType);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("imageUrl", url);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir imagen: " + ex.getMessage());
        }
    }

    @PostMapping("/delete-image")
    public ResponseEntity<Map<String, Object>> eliminarImagen(@RequestBody DeleteImageDto body) {
        try {
            String imageUrl = body.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen no proporcionada");
            }

            String[] parts = imageUrl.split("/productos/");
            if (parts.length < 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }

            String path = parts[1];
            int slash = path.indexOf('/');
            if (slash < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }
            String id = path.substring(0, slash);
            String fileName = path.substring(slash + 1);

            gestionProductosService.eliminarImagen(id, fileName);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Imagen eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Map<String, Object> mapProductoToResponse(Producto producto) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id_producto", producto.getId());
        response.put("sku", producto.getSku());
        response.put("id_categoria", producto.getIdCategoria());
        response.put("nombre", producto.getNombre());
        response.put("precio_unidad", producto.getPrecioUnidad());
        response.put("precio_caja", producto.getPrecioCaja());
        response.put("unidades_por_caja", producto.getUnidadesPorCaja());
        response.put("stock_almacen_central", producto.getStockAlmacenCentral());
        response.put("descripcion", producto.getDescripcion());
        response.put("url_imagen", producto.getUrlImagen());
        response.put("estado", producto.getEstado());
        if (producto.getCreatedAt() != null) {
            response.put("created_at", producto.getCreatedAt().toString());
        }
        return response;
    }
}
