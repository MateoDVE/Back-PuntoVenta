package com.back.puntoventa.app.config;

import com.back.puntoventa.app.domain.auth.port.AutenticacionPort;
import com.back.puntoventa.app.domain.auth.port.UsuarioRepositoryPort;
import com.back.puntoventa.app.domain.auth.service.AutorizacionAuthService;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import com.back.puntoventa.app.domain.productos.port.StoragePort;
import com.back.puntoventa.app.domain.productos.service.GestionProductosService;
import com.back.puntoventa.app.domain.vendedores.port.GestorUsuariosPort;
import com.back.puntoventa.app.domain.vendedores.port.VendedorRepositoryPort;
import com.back.puntoventa.app.domain.vendedores.service.GestionVendedoresService;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseAutenticacionAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseGestorUsuariosAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseProductoRepositoryAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseStorageAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseUsuarioRepositoryAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.adapter.SupabaseVendedorRepositoryAdapter;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la arquitectura Hexagonal.
 * Aquí se wirea (inyecta) todas las implementaciones de puertos (adapters)
 * a los servicios de dominio que dependen de ellas.
 */
@Configuration
public class HexagonalArchitectureConfig {

    /**
     * Configura los adapters que implementan puertos.
     * Los adapters son componentes Spring (@Component) pero se exponen
     * aquí explícitamente para documentación y control preciso.
     */
    @Bean
    public AutenticacionPort autenticacionPort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseAutenticacionAdapter(supabaseHttpClient);
    }

    @Bean
    public UsuarioRepositoryPort usuarioRepositoryPort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseUsuarioRepositoryAdapter(supabaseHttpClient);
    }

    @Bean
    public GestorUsuariosPort gestorUsuariosPort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseGestorUsuariosAdapter(supabaseHttpClient);
    }

    @Bean
    public VendedorRepositoryPort vendedorRepositoryPort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseVendedorRepositoryAdapter(supabaseHttpClient);
    }

    @Bean
    public ProductoRepositoryPort productoRepositoryPort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseProductoRepositoryAdapter(supabaseHttpClient);
    }

    @Bean
    public StoragePort storagePort(SupabaseHttpClient supabaseHttpClient) {
        return new SupabaseStorageAdapter(supabaseHttpClient);
    }

    /**
     * Configura los servicios de dominio (use cases).
     * Estos dependen de los puertos (interfaces), no de implementaciones concretas.
     * Esto permite cambiar la implementación sin modificar la lógica de dominio.
     */
    @Bean
    public AutorizacionAuthService autorizacionAuthService(
            AutenticacionPort autenticacionPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        return new AutorizacionAuthService(autenticacionPort, usuarioRepositoryPort);
    }

    @Bean
    public GestionVendedoresService gestionVendedoresService(
            VendedorRepositoryPort vendedorRepositoryPort,
            GestorUsuariosPort gestorUsuariosPort) {
        return new GestionVendedoresService(vendedorRepositoryPort, gestorUsuariosPort);
    }

    @Bean
    public GestionProductosService gestionProductosService(
            ProductoRepositoryPort productoRepositoryPort,
            StoragePort storagePort) {
        return new GestionProductosService(productoRepositoryPort, storagePort);
    }
}
