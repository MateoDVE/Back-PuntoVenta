package com.back.puntoventa.app.domain.cierre.port;

import com.back.puntoventa.app.domain.cierre.model.CierreJornada;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: contrato de persistencia para cierres de jornada.
 * El dominio depende de esta interfaz, no de la implementación concreta.
 */
public interface CierreJornadaRepositoryPort {

    CierreJornada crear(CierreJornada cierre);

    Optional<CierreJornada> obtenerPorId(String id);

    Optional<CierreJornada> obtenerPorVendedorYFecha(UUID idVendedor, LocalDate fecha);

    List<CierreJornada> obtenerPorVendedor(UUID idVendedor);

    List<CierreJornada> obtenerTodos();

    CierreJornada actualizar(String id, CierreJornada cierre);

    void eliminar(String id);

    boolean existePorVendedorYFecha(UUID idVendedor, LocalDate fecha);
}
