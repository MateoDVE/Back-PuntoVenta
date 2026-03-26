package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.productos.port.StoragePort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario: Implementa StoragePort usando Supabase Storage.
 */
@Component
public class SupabaseStorageAdapter implements StoragePort {

    private static final String BUCKET = "productos";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabaseStorageAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public String uploadProductImage(String productId, String fileName, byte[] content, String mimeType) {
        String path = productId + "/" + fileName;
        supabaseHttpClient.uploadToStorage(BUCKET, path, content, mimeType);
        return getPublicUrl(productId, fileName);
    }

    @Override
    public byte[] downloadProductImage(String productId, String fileName) {
        // Nota: SupabaseHttpClient no tiene método para descargar archivos binarios.
        // Esto sería una extensión futura si se necesita.
        throw new UnsupportedOperationException("Descarga de imágenes no soportada en este momento");
    }

    @Override
    public void deleteProductImage(String productId, String fileName) {
        String path = productId + "/" + fileName;
        supabaseHttpClient.deleteFromStorage(BUCKET, path);
    }

    @Override
    public String getPublicUrl(String productId, String fileName) {
        String path = productId + "/" + fileName;
        return supabaseHttpClient.buildPublicStorageUrl(BUCKET, path);
    }
}
