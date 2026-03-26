package com.back.puntoventa.app.domain.productos.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import com.back.puntoventa.app.domain.productos.port.StoragePort;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: Gestión de productos.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionProductosService {

    private static final Logger logger = LoggerFactory.getLogger(GestionProductosService.class);
    private final ProductoRepositoryPort productoRepositoryPort;
    private final StoragePort storagePort;

    public GestionProductosService(ProductoRepositoryPort productoRepositoryPort, StoragePort storagePort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.storagePort = storagePort;
    }

    public Producto crearProducto(String sku, Integer idCategoria, String nombre, String descripcion,
            Double precioUnidad, Double precioCaja, Integer unidadesPorCaja, Integer stockAlmacenCentral) {
        logger.debug("Creando producto - SKU: {}, Nombre: {}", sku, nombre);
        sku = sku == null ? "" : sku.trim();
        nombre = nombre == null ? "" : nombre.trim();

        if (sku.isBlank()) {
            logger.warn("Intento de crear producto con SKU vacío");
            throw new DomainException("SKU no puede estar vacío");
        }
        if (nombre.isBlank()) {
            logger.warn("Intento de crear producto con nombre vacío");
            throw new DomainException("Nombre no puede estar vacío");
        }
        if (precioUnidad == null || precioUnidad <= 0) {
            logger.warn("Intento de crear producto con precio inválido: {}", precioUnidad);
            throw new DomainException("Precio unidad debe ser mayor a 0");
        }

        if (productoRepositoryPort.existeSku(sku, null)) {
            logger.warn("Intento de crear producto con SKU existente: {}", sku);
            throw new DomainException("Ya existe un producto con ese SKU");
        }

        Producto producto = new Producto(
                null,
                sku,
                idCategoria,
                nombre,
                precioUnidad,
                precioCaja,
                unidadesPorCaja,
                stockAlmacenCentral,
                descripcion,
                null,
                "ACTIVO",
                LocalDateTime.now());
        producto = productoRepositoryPort.crear(producto);
        logger.info("Producto creado exitosamente - ID: {}, SKU: {}, Nombre: {}", producto.getId(), sku, nombre);
        return producto;
    }

    public List<Producto> obtenerTodos() {
        logger.debug("Obteniendo todos los productos");
        List<Producto> productos = productoRepositoryPort.obtenerTodos();
        logger.debug("Se obtuvieron {} productos", productos.size());
        return productos;
    }

    public Producto obtenerPorId(String id) {
        logger.debug("Obteniendo producto por ID: {}", id);
        Producto producto = productoRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado - ID: {}", id);
                    return new DomainException("Producto no encontrado");
                });
        logger.debug("Producto encontrado - ID: {}, SKU: {}", id, producto.getSku());
        return producto;
    }

    public Producto actualizar(String id, String sku, Integer idCategoria, String nombre, String descripcion,
            String urlImagen, Double precioUnidad, Double precioCaja, Integer unidadesPorCaja,
            Integer stockAlmacenCentral, String estado) {
        logger.debug("Actualizando producto - ID: {}", id);
        Producto productoActual = obtenerPorId(id);

        sku = sku == null || sku.isBlank() ? productoActual.getSku() : sku.trim();
        idCategoria = idCategoria == null ? productoActual.getIdCategoria() : idCategoria;
        nombre = nombre == null || nombre.isBlank() ? productoActual.getNombre() : nombre.trim();
        precioUnidad = precioUnidad == null || precioUnidad <= 0 ? productoActual.getPrecioUnidad() : precioUnidad;
        precioCaja = precioCaja == null ? productoActual.getPrecioCaja() : precioCaja;
        unidadesPorCaja = unidadesPorCaja == null ? productoActual.getUnidadesPorCaja() : unidadesPorCaja;
        stockAlmacenCentral = stockAlmacenCentral == null ? productoActual.getStockAlmacenCentral()
                : stockAlmacenCentral;
        descripcion = descripcion == null ? productoActual.getDescripcion() : descripcion;
        urlImagen = urlImagen == null ? productoActual.getUrlImagen() : urlImagen;
        estado = estado == null || estado.isBlank() ? productoActual.getEstado() : estado.trim().toUpperCase();

        Producto productoActualizado = new Producto(
                id,
                sku,
                idCategoria,
                nombre,
                precioUnidad,
                precioCaja,
                unidadesPorCaja,
                stockAlmacenCentral,
                descripcion,
                urlImagen,
                estado,
                productoActual.getCreatedAt());
        logger.debug("Cambios de producto validados - ID: {}", id);

        Producto result = productoRepositoryPort.actualizar(id, productoActualizado);
        logger.info("Producto actualizado exitosamente - ID: {}, SKU: {}", id, sku);
        return result;
    }

    public void eliminar(String id) {
        logger.debug("Eliminando producto - ID: {}", id);
        obtenerPorId(id); // Valida que exista
        productoRepositoryPort.eliminar(id);
        logger.info("Producto eliminado exitosamente - ID: {}", id);
    }

    public String subirImagen(String productoId, String fileName, byte[] content, String mimeType) {
        logger.debug("Subiendo imagen para producto - ID: {}, Archivo: {}", productoId, fileName);
        Producto producto = obtenerPorId(productoId);

        if (content == null || content.length == 0) {
            logger.warn("Intento de subir imagen vacía para producto ID: {}", productoId);
            throw new DomainException("Archivo de imagen vacío");
        }

        // Validar que sea una imagen
        if (!mimeType.toLowerCase().startsWith("image/")) {
            logger.warn("Tipo MIME inválido para imagen en producto ID: {} - MimeType: {}", productoId, mimeType);
            throw new DomainException("El archivo debe ser una imagen");
        }

        String url = storagePort.uploadProductImage(productoId, fileName, content, mimeType);
        logger.debug("Imagen subida al storage: {}", url);

        // Actualizar el URL de imagen del producto
        Producto productoConImagen = new Producto(
                producto.getId(),
                producto.getSku(),
                producto.getIdCategoria(),
                producto.getNombre(),
                producto.getPrecioUnidad(),
                producto.getPrecioCaja(),
                producto.getUnidadesPorCaja(),
                producto.getStockAlmacenCentral(),
                producto.getDescripcion(),
                url,
                producto.getEstado(),
                producto.getCreatedAt());
        productoRepositoryPort.actualizar(productoId, productoConImagen);
        logger.info("Imagen asociada al producto exitosamente - ID: {}, FileName: {}", productoId, fileName);

        return url;
    }

    public String subirImagenTemporal(String fileName, byte[] content, String mimeType) {
        logger.debug("Subiendo imagen temporal - Archivo: {}", fileName);
        if (content == null || content.length == 0) {
            logger.warn("Intento de subir imagen temporal vacía");
            throw new DomainException("Archivo de imagen vacío");
        }

        if (mimeType == null || !mimeType.toLowerCase().startsWith("image/")) {
            logger.warn("Tipo MIME inválido para imagen temporal - MimeType: {}", mimeType);
            throw new DomainException("El archivo debe ser una imagen");
        }

        // Se usa carpeta "temp" para imágenes aún no asociadas a un producto creado.
        String url = storagePort.uploadProductImage("temp", fileName, content, mimeType);
        logger.info("Imagen temporal subida exitosamente: {}", url);
        return url;
    }

    public byte[] descargarImagen(String productoId, String fileName) {
        logger.debug("Descargando imagen - ProductoID: {}, FileName: {}", productoId, fileName);
        obtenerPorId(productoId);
        byte[] imagenBytes = storagePort.downloadProductImage(productoId, fileName);
        logger.debug("Imagen descargada exitosamente - Tamaño: {} bytes", imagenBytes.length);
        return imagenBytes;
    }

    public void eliminarImagen(String productoId, String fileName) {
        logger.debug("Eliminando imagen - ProductoID: {}, FileName: {}", productoId, fileName);
        obtenerPorId(productoId);
        storagePort.deleteProductImage(productoId, fileName);
        logger.info("Imagen eliminada exitosamente - ProductoID: {}, FileName: {}", productoId, fileName);
    }
}
