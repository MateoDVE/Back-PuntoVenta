package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.inventario.model.AsignacionStock;
import com.back.puntoventa.app.domain.inventario.port.AsignacionRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SupabaseAsignacionRepositoryAdapter implements AsignacionRepositoryPort {

    private final SupabaseHttpClient supabaseHttpClient;
    private static final String TABLE = "carga_transporte"; // Nombre según tu planeación

    public SupabaseAsignacionRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public AsignacionStock guardar(AsignacionStock asignacion) {
        Map<String, Object> row = new HashMap<>();
        row.put("id_vendedor", asignacion.getIdVendedor());
        row.put("id_producto", asignacion.getIdProducto());
        row.put("cantidad_inicial", asignacion.getCantidadAsignada());
        row.put("estado_validacion", asignacion.getEstadoValidacion());

        // Usamos el cliente que ya existe en el proyecto
        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, row, null, "return=representation");
        
        return asignacion; // En una fase real, mapearíamos el resultado de la DB
    }

    @Override
    public List<AsignacionStock> obtenerPorVendedor(String idVendedor) {
        Map<String, String> query = new HashMap<>();
        query.put("id_vendedor", "eq." + idVendedor);
        // Lógica para obtener las cargas desde Supabase
        return List.of(); 
    }

    @Override
    public Optional<AsignacionStock> obtenerPorId(String idCarga) {
        return Optional.empty();
    }

    @Override
    public AsignacionStock actualizarEstado(String idCarga, String nuevoEstado) {
        return null;
    }
}

