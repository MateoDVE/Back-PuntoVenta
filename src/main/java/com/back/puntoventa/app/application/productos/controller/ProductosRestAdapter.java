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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador primario (HTTP adapter): Expone los casos de uso de gestión de
 * productos
 * del dominio al mundo exterior.
 */
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductosRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProductosRestAdapter.class);
    private final GestionProductosService gestionProductosService;

    public ProductosRestAdapter(GestionProductosService gestionProductosService) {
        this.gestionProductosService = gestionProductosService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody CreateProductoDto request) {
        logger.info("POST /productos - Crear nuevo producto: {}", request.getNombre());
        try {
            Producto producto = gestionProductosService.crearProducto(
                    request.getSku(),
                    request.getIdCategoria(),
                    request.getNombre(),
                    request.getDescripcion(),
                    request.getPrecioUnidad(),
                    request.getPrecioCaja(),
                    request.getUnidadesPorCaja(),
                    request.getStockAlmacenCentral());
            logger.info("Producto creado exitosamente - ID: {}, SKU: {}, Nombre: {}", producto.getId(),
                    producto.getSku(), producto.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            logger.warn("Error de dominio al crear producto: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al crear producto: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al crear producto", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        logger.info("GET /productos - Obtener todos los productos");
        try {
            List<Producto> productos = gestionProductosService.obtenerTodos();
            logger.debug("Se obtuvieron {} productos", productos.size());
            List<Map<String, Object>> response = productos.stream()
                    .map(this::mapProductoToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al obtener productos", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Map<String, Object>>> obtenerStockBajo(
            @RequestParam(name = "umbral", defaultValue = "100") Integer umbral) {
        logger.info("GET /productos/stock-bajo - Consultando alertas con umbral: {}", umbral);
        try {
            List<Producto> productos = gestionProductosService.obtenerStockBajo(umbral);
            List<Map<String, Object>> response = productos.stream()
                    .map(this::mapProductoToResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al obtener productos con stock bajo", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        logger.info("GET /productos/{} - Obtener producto por ID", id);
        try {
            Producto producto = gestionProductosService.obtenerPorId(id);
            logger.debug("Producto encontrado - ID: {}, SKU: {}", id, producto.getSku());
            return ResponseEntity.ok(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            logger.warn("Producto no encontrado - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Producto no encontrado - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String id,
            @RequestBody UpdateProductoDto request) {
        logger.info("PUT /productos/{} - Actualizar producto", id);
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
                    request.getEstado());
            logger.info("Producto actualizado exitosamente - ID: {}", id);
            return ResponseEntity.ok(mapProductoToResponse(producto));
        } catch (DomainException ex) {
            logger.warn("Error de dominio al actualizar producto - ID: {}, Motivo: {}", id, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumento inválido al actualizar producto - ID: {}, Motivo: {}", id, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al actualizar producto - ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        logger.info("DELETE /productos/{} - Eliminar producto", id);
        try {
            gestionProductosService.eliminar(id);
            logger.info("Producto eliminado exitosamente - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (DomainException ex) {
            logger.warn("Producto no encontrado para eliminar - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Producto no encontrado para eliminar - ID: {}", id);
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al eliminar producto - ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> subirImagenSinId(
            @RequestParam("image") MultipartFile file) {
        logger.info("POST /productos/upload - Subir imagen temporal, nombre archivo: {}", file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                logger.warn("Intento de subir archivo vacío");
                throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
            }

            String fileName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            byte[] content = file.getBytes();

            String url = gestionProductosService.subirImagenTemporal(fileName, content, mimeType);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("imageUrl", url);
            logger.info("Imagen temporal subida exitosamente: {}", fileName);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al subir imagen temporal", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir imagen: " + ex.getMessage());
        }
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<Map<String, Object>> subirImagen(
            @PathVariable String id,
            @RequestParam("image") MultipartFile file) {
        logger.info("POST /productos/upload/{} - Subir imagen para producto, archivo: {}", id,
                file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                logger.warn("Intento de subir archivo vacío para producto ID: {}", id);
                throw new ApiException(HttpStatus.BAD_REQUEST, "Archivo vacío");
            }

            String fileName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            byte[] content = file.getBytes();

            String url = gestionProductosService.subirImagen(id, fileName, content, mimeType);
            logger.info("Imagen subida exitosamente para producto ID: {}", id);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("imageUrl", url);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Error al subir imagen para producto ID: {} - {}", id, ex.getMessage());
            throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error al subir imagen para producto ID: {}", id, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir imagen: " + ex.getMessage());
        }
    }

    @PostMapping("/delete-image")
    public ResponseEntity<Map<String, Object>> eliminarImagen(@RequestBody DeleteImageDto body) {
        logger.info("POST /productos/delete-image - Eliminar imagen");
        try {
            String imageUrl = body.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                logger.warn("Intento de eliminar imagen sin URL proporcionada");
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen no proporcionada");
            }

            String[] parts = imageUrl.split("/productos/");
            if (parts.length < 2) {
                logger.warn("URL de imagen inválida: {}", imageUrl);
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }

            String path = parts[1];
            int slash = path.indexOf('/');
            if (slash < 0) {
                logger.warn("URL de imagen inválida (sin slash): {}", imageUrl);
                throw new ApiException(HttpStatus.BAD_REQUEST, "URL de imagen inválida");
            }
            String id = path.substring(0, slash);
            String fileName = path.substring(slash + 1);

            gestionProductosService.eliminarImagen(id, fileName);
            logger.info("Imagen eliminada exitosamente - ProductoID: {}, FileName: {}", id, fileName);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Imagen eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Error al eliminar imagen: {}", ex.getMessage());
            throw new ApiException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error interno al eliminar imagen", ex);
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
