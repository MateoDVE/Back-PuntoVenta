package com.back.puntoventa.app.application.sucursales.controller;

import com.back.puntoventa.app.application.sucursales.model.Sucursal;
import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sucursales")
@CrossOrigin(origins = "http://localhost:4200")
public class SucursalRestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SucursalRestAdapter.class);
    private static final String TABLE = "sucursales";
    
    private final SupabaseHttpClient supabaseHttpClient;

    public SucursalRestAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    private Sucursal mapToSucursal(Map<String, Object> row) {
        Sucursal s = new Sucursal();
        s.setId((String) row.get("id"));
        s.setNombre((String) row.get("nombre"));
        
        if (row.get("latitud") != null) {
            s.setLatitud(((Number) row.get("latitud")).doubleValue());
        }
        if (row.get("longitud") != null) {
            s.setLongitud(((Number) row.get("longitud")).doubleValue());
        }
        
        s.setEsPrincipal(row.get("es_principal") != null && (Boolean) row.get("es_principal"));
        s.setFechaCreacion((String) row.get("fecha_creacion"));
        return s;
    }

    private void desmarcarOtrasPrincipales(String idExcluido) {
        logger.info("Desmarcando otras sucursales como principal");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("es_principal", "eq.true");
        queryParams.put("select", "*");
        
        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
        for (Map<String, Object> row : rows) {
            String id = (String) row.get("id");
            if (idExcluido != null && id.equals(idExcluido)) {
                continue;
            }
            
            Map<String, String> updateParams = new HashMap<>();
            updateParams.put("id", "eq." + id);
            
            Map<String, Object> body = new HashMap<>();
            body.put("es_principal", false);
            
            supabaseHttpClient.update(TABLE, updateParams, body);
        }
    }

    @GetMapping
    public ResponseEntity<List<Sucursal>> obtenerTodas() {
        logger.info("GET /sucursales - Obtener todas las sucursales de la base de datos");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("order", "id.asc");
        queryParams.put("select", "*");

        try {
            List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
            List<Sucursal> list = rows.stream()
                    .map(this::mapToSucursal)
                    .toList();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error al obtener sucursales de Supabase", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudieron obtener las sucursales de la base de datos");
        }
    }

    @PostMapping
    public ResponseEntity<Sucursal> crear(@RequestBody Sucursal request) {
        logger.info("POST /sucursales - Crear sucursal: {}", request.getNombre());
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre de la sucursal es obligatorio");
        }
        if (request.getLatitud() == null || request.getLongitud() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Las coordenadas (latitud y longitud) son obligatorias");
        }

        try {
            Map<String, String> selectParams = new HashMap<>();
            selectParams.put("select", "id");
            List<Map<String, Object>> existing = supabaseHttpClient.select(TABLE, selectParams);

            boolean esLaPrimera = existing.isEmpty();
            boolean esPrincipal = request.isEsPrincipal() || esLaPrimera;

            if (esPrincipal) {
                desmarcarOtrasPrincipales(null);
            }

            String id = UUID.randomUUID().toString();
            String fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Map<String, Object> body = new HashMap<>();
            body.put("id", id);
            body.put("nombre", request.getNombre().trim());
            body.put("latitud", request.getLatitud());
            body.put("longitud", request.getLongitud());
            body.put("es_principal", esPrincipal);
            body.put("fecha_creacion", fechaCreacion);

            List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE, body, null, "return=representation");
            if (result.isEmpty()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la sucursal");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(mapToSucursal(result.get(0)));
        } catch (Exception ex) {
            logger.error("Error al crear sucursal en Supabase", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al persistir la sucursal en la base de datos");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable String id, @RequestBody Sucursal request) {
        logger.info("PUT /sucursales/{} - Actualizar sucursal", id);
        
        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("id", "eq." + id);
            queryParams.put("select", "*");
            
            List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
            if (rows.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Sucursal no encontrada");
            }

            Map<String, Object> body = new HashMap<>();
            if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
                body.put("nombre", request.getNombre().trim());
            }
            if (request.getLatitud() != null && request.getLongitud() != null) {
                body.put("latitud", request.getLatitud());
                body.put("longitud", request.getLongitud());
            }
            
            if (request.isEsPrincipal()) {
                desmarcarOtrasPrincipales(id);
                body.put("es_principal", true);
            } else {
                body.put("es_principal", request.isEsPrincipal());
            }

            List<Map<String, Object>> result = supabaseHttpClient.update(TABLE, queryParams, body);
            if (result.isEmpty()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo actualizar la sucursal");
            }

            return ResponseEntity.ok(mapToSucursal(result.get(0)));
        } catch (Exception ex) {
            logger.error("Error al actualizar sucursal en Supabase", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar la sucursal en la base de datos");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable String id) {
        logger.info("DELETE /sucursales/{} - Eliminar sucursal", id);
        
        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("id", "eq." + id);
            queryParams.put("select", "*");
            
            List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE, queryParams);
            if (rows.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Sucursal no encontrada");
            }

            Sucursal encontrada = mapToSucursal(rows.get(0));
            boolean eraPrincipal = encontrada.isEsPrincipal();

            supabaseHttpClient.deleteRows(TABLE, queryParams);

            if (eraPrincipal) {
                Map<String, String> selectParams = new HashMap<>();
                selectParams.put("select", "*");
                selectParams.put("limit", "1");
                List<Map<String, Object>> remaining = supabaseHttpClient.select(TABLE, selectParams);
                if (!remaining.isEmpty()) {
                    String nextId = (String) remaining.get(0).get("id");
                    Map<String, String> updateParams = new HashMap<>();
                    updateParams.put("id", "eq." + nextId);
                    
                    Map<String, Object> updateBody = new HashMap<>();
                    updateBody.put("es_principal", true);
                    
                    supabaseHttpClient.update(TABLE, updateParams, updateBody);
                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Sucursal eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error al eliminar sucursal en Supabase", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la sucursal de la base de datos");
        }
    }
}
