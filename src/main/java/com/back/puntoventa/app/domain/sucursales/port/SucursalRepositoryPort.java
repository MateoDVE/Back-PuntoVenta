package com.back.puntoventa.app.domain.sucursales.port;

import com.back.puntoventa.app.domain.sucursales.model.Sucursal;
import java.util.List;
import java.util.Optional;

public interface SucursalRepositoryPort {

    List<Sucursal> obtenerTodas();

    Optional<Sucursal> obtenerPorId(String id);

    Sucursal crear(Sucursal sucursal);

    Sucursal actualizar(String id, Sucursal sucursal);

    void eliminar(String id);
}
