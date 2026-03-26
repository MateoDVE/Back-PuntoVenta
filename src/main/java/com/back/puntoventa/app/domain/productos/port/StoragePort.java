package com.back.puntoventa.app.domain.productos.port;

/**
 * Puerto: Contrato para almacenamiento de imágenes de productos.
 * Abstrae la implementación (Supabase Storage, S3, etc.)
 */
public interface StoragePort {
    /**
     * Sube una imagen de producto al almacenamiento.
     * @param productId ID del producto (usado para organizar carpetas)
     * @param fileName nombre del archivo
     * @param content contenido binario de la imagen
     * @param mimeType tipo MIME (image/jpeg, image/png, etc.)
     * @return URL pública de la imagen
     */
    String uploadProductImage(String productId, String fileName, byte[] content, String mimeType);

    /**
     * Descarga una imagen de producto.
     * @param productId ID del producto
     * @param fileName nombre del archivo
     * @return contenido binario
     */
    byte[] downloadProductImage(String productId, String fileName);

    /**
     * Elimina una imagen de producto.
     * @param productId ID del producto
     * @param fileName nombre del archivo
     */
    void deleteProductImage(String productId, String fileName);

    /**
     * Obtiene la URL pública de una imagen.
     * @param productId ID del producto
     * @param fileName nombre del archivo
     * @return URL pública
     */
    String getPublicUrl(String productId, String fileName);
}
