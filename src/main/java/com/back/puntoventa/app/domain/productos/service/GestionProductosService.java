package com.back.puntoventa.app.domain.productos.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import com.back.puntoventa.app.domain.productos.port.StoragePort;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso: Gestión de productos.
 * Lógica pura sin dependencias a Spring ni Supabase.
 */
public class GestionProductosService {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final StoragePort storagePort;

    public GestionProductosService(ProductoRepositoryPort productoRepositoryPort, StoragePort storagePort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.storagePort = storagePort;
    }

    public Producto crearProducto(String sku, Integer idCategoria, String nombre, String descripcion,
            Double precioUnidad, Double precioCaja, Integer unidadesPorCaja, Integer stockAlmacenCentral) {
        sku = sku == null ? "" : sku.trim();
        nombre = nombre == null ? "" : nombre.trim();

        if (sku.isBlank()) {
            throw new DomainException("SKU no puede estar vacío");
        }
        if (nombre.isBlank()) {
            throw new DomainException("Nombre no puede estar vacío");
        }
        if (precioUnidad == null || precioUnidad <= 0) {
            throw new DomainException("Precio unidad debe ser mayor a 0");
        }

        if (productoRepositoryPort.existeSku(sku, null)) {
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
            LocalDateTime.now()
        );
        return productoRepositoryPort.crear(producto);
    }

    public List<Producto> obtenerTodos() {
        return productoRepositoryPort.obtenerTodos();
    }

    public Producto obtenerPorId(String id) {
        return productoRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new DomainException("Producto no encontrado"));
    }

        public Producto actualizar(String id, String sku, Integer idCategoria, String nombre, String descripcion,
            String urlImagen, Double precioUnidad, Double precioCaja, Integer unidadesPorCaja,
            Integer stockAlmacenCentral, String estado) {
        Producto productoActual = obtenerPorId(id);

        sku = sku == null || sku.isBlank() ? productoActual.getSku() : sku.trim();
        idCategoria = idCategoria == null ? productoActual.getIdCategoria() : idCategoria;
        nombre = nombre == null || nombre.isBlank() ? productoActual.getNombre() : nombre.trim();
        precioUnidad = precioUnidad == null || precioUnidad <= 0 ? productoActual.getPrecioUnidad() : precioUnidad;
        precioCaja = precioCaja == null ? productoActual.getPrecioCaja() : precioCaja;
        unidadesPorCaja = unidadesPorCaja == null ? productoActual.getUnidadesPorCaja() : unidadesPorCaja;
        stockAlmacenCentral = stockAlmacenCentral == null ? productoActual.getStockAlmacenCentral() : stockAlmacenCentral;
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
                productoActual.getCreatedAt()
        );

        return productoRepositoryPort.actualizar(id, productoActualizado);
    }

    public void eliminar(String id) {
        obtenerPorId(id);
        productoRepositoryPort.eliminar(id);
    }

    public String subirImagen(String productoId, String fileName, byte[] content, String mimeType) {
        Producto producto = obtenerPorId(productoId);
        
        if (content == null || content.length == 0) {
            throw new DomainException("Archivo de imagen vacío");
        }

        // Validar que sea una imagen
        if (!mimeType.toLowerCase().startsWith("image/")) {
            throw new DomainException("El archivo debe ser una imagen");
        }

        String url = storagePort.uploadProductImage(productoId, fileName, content, mimeType);
        
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
                producto.getCreatedAt()
        );
        productoRepositoryPort.actualizar(productoId, productoConImagen);

        return url;
    }

    public String subirImagenTemporal(String fileName, byte[] content, String mimeType) {
        if (content == null || content.length == 0) {
            throw new DomainException("Archivo de imagen vacío");
        }

        if (mimeType == null || !mimeType.toLowerCase().startsWith("image/")) {
            throw new DomainException("El archivo debe ser una imagen");
        }

        // Se usa carpeta "temp" para imágenes aún no asociadas a un producto creado.
        return storagePort.uploadProductImage("temp", fileName, content, mimeType);
    }

    public byte[] descargarImagen(String productoId, String fileName) {
        obtenerPorId(productoId);
        return storagePort.downloadProductImage(productoId, fileName);
    }

    public void eliminarImagen(String productoId, String fileName) {
        obtenerPorId(productoId);
        storagePort.deleteProductImage(productoId, fileName);
    }
}
