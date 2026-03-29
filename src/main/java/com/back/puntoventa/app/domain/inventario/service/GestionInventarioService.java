package com.back.puntoventa.app.domain.inventario.service;

import com.back.puntoventa.app.domain.common.exception.DomainException;
import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.port.AsignacionRepositoryPort;
import com.back.puntoventa.app.domain.productos.model.Producto;
import com.back.puntoventa.app.domain.productos.port.ProductoRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
public class GestionInventarioService {

    private final AsignacionRepositoryPort asignacionRepository;
    private final ProductoRepositoryPort productoRepository;

    public GestionInventarioService(AsignacionRepositoryPort asignacionRepository,
                                   ProductoRepositoryPort productoRepository) {
        this.asignacionRepository = asignacionRepository;
        this.productoRepository = productoRepository;
    }
    public AsignacionStock asignarStockAVendedor(String productoId, String vendedorId, Integer cantidad) {
        Producto producto = productoRepository.obtenerPorId(productoId)
                .orElseThrow(() -> new DomainException("Producto no encontrado"));
        if (producto.getStockAlmacenCentral() < cantidad) {
            throw new DomainException("Stock insuficiente en almacén central. Disponible: " + producto.getStockAlmacenCentral());
        }
        AsignacionStock nuevaAsignacion = new AsignacionStock(
                null, 
                vendedorId, 
                productoId, 
                cantidad, 
                "PENDIENTE", 
                LocalDateTime.now()
        );
        AsignacionStock resultado = asignacionRepository.guardar(nuevaAsignacion);
        return resultado;
    }

    public List<AsignacionStock> obtenerCargasPorVendedor(String vendedorId) {
        return asignacionRepository.obtenerPorVendedor(vendedorId);
    }
}