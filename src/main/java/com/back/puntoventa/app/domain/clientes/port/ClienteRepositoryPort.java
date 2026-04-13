package com.back.puntoventa.app.domain.clientes.port;

import com.back.puntoventa.app.domain.clientes.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {

    List<Cliente> obtenerTodos();

    Optional<Cliente> obtenerPorId(String id);

    List<Cliente> obtenerPorVendedor(String idVendedor);

    Optional<Cliente> obtenerPorCiNit(String ciNit);

    Cliente crear(Cliente cliente);

    Cliente actualizar(String id, Cliente cliente);

    void eliminar(String id);

    boolean existeCiNit(String ciNit, String excludeId);
}
