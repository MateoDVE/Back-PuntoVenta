package com.back.puntoventa.app.domain.ruta.port;

import com.back.puntoventa.app.domain.ruta.model.Ubicacion;
import java.util.List;

public interface RutaRepositoryPort {
    void guardarUbicacion(Ubicacion ubicacion);
    List<Ubicacion> obtenerHistorialPorVendedor(String idVendedor);
}
