package com.back.puntoventa.app.domain.inventario_ruta.port;

import com.back.puntoventa.app.domain.inventario_ruta.model.CargaTransporte;
import com.back.puntoventa.app.domain.productos.model.Producto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto: Contrato para persistencia de inventario de ruta.
 */
public interface InventarioRutaRepositoryPort {
    boolean vendedorExiste(UUID idVendedor);
    List<CargaTransporte> obtenerCargasPorVendedorYFecha(UUID idVendedor, LocalDate fecha);
    List<Producto> obtenerProductosPorIds(List<String> ids);
    void actualizarCantidadActual(Integer idCarga, Integer cantidadActual);
    void validarCargas(List<Integer> idsCargas);
}