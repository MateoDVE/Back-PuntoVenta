package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.ruta.model.Ubicacion;
import com.back.puntoventa.app.domain.ruta.port.RutaRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;import java.util.List;import java.util.List;
import java.util.Map;

@Component
public class SupabaseRutaAdapter implements RutaRepositoryPort {

    private final SupabaseHttpClient supabaseHttpClient;
    private static final String TABLE = "ubicaciones"; // Asumiendo el nombre de la tabla

    public SupabaseRutaAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

   @Override
public void guardarUbicacion(Ubicacion ubicacion) {
    // 1. Preparamos el mapa de datos
    Map<String, Object> data = new HashMap<>();
    data.put("id_vendedor", ubicacion.getIdVendedor());
    data.put("latitud", ubicacion.getLatitud());
    data.put("longitud", ubicacion.getLongitud());
    data.put("fecha_registro", ubicacion.getFechaRegistro() != null ? ubicacion.getFechaRegistro().toString() : java.time.LocalDateTime.now().toString());
    supabaseHttpClient.insert("historial_ubicacion", data, "id_seguimiento", "*");
    
    System.out.println("LOG: Coordenadas enviadas a la tabla historial_ubicacion.");
}

    @Override
    public List<Ubicacion> obtenerHistorialPorVendedor(String idVendedor) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_vendedor", "eq." + idVendedor);
        
        List<Map<String, Object>> results = supabaseHttpClient.select(TABLE, queryParams);
        return results.stream()
            .map(this::mapToUbicacion)
            .toList();
    }
    
    private Ubicacion mapToUbicacion(Map<String, Object> data) {
        return new Ubicacion(
            (String) data.get("id_vendedor"),
            (Double) data.get("latitud"),
            (Double) data.get("longitud")
        );
    }
}
