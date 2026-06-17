package com.back.puntoventa.app.infrastructure.persistence.supabase.adapter;

import com.back.puntoventa.app.domain.pedidos.model.DetallePedidoProgramado;
import com.back.puntoventa.app.domain.pedidos.model.EstadoPedido;
import com.back.puntoventa.app.domain.pedidos.model.PedidoProgramado;
import com.back.puntoventa.app.domain.pedidos.model.PrioridadPedido;
import com.back.puntoventa.app.domain.pedidos.port.PedidoProgramadoRepositoryPort;
import com.back.puntoventa.app.infrastructure.persistence.supabase.client.SupabaseHttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador secundario para persistir Pedidos Programados en Supabase.
 */
@Component
public class SupabasePedidoProgramadoRepositoryAdapter implements PedidoProgramadoRepositoryPort {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TABLE_PEDIDOS = "pedidos_programados";
    private static final String TABLE_DETALLES = "detalle_pedido_programado";

    private final SupabaseHttpClient supabaseHttpClient;

    public SupabasePedidoProgramadoRepositoryAdapter(SupabaseHttpClient supabaseHttpClient) {
        this.supabaseHttpClient = supabaseHttpClient;
    }

    @Override
    public PedidoProgramado crear(PedidoProgramado pedido) {
        Map<String, Object> body = new HashMap<>();
        body.put("id_cliente", pedido.getIdCliente());
        body.put("id_vendedor", pedido.getIdVendedor());
        body.put("fecha_programada", pedido.getFechaProgramada().toString());
        body.put("prioridad", pedido.getPrioridad().name());
        body.put("estado", pedido.getEstado().name());
        body.put("observaciones", pedido.getObservaciones());
        if (pedido.getCreatedAt() != null) {
            body.put("created_at", pedido.getCreatedAt().format(ISO_FORMATTER));
        }

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE_PEDIDOS, body, null, "return=representation");
        if (result.isEmpty()) {
            throw new RuntimeException("Error al insertar cabecera de pedido programado en Supabase");
        }

        return mapToPedidoProgramado(result.get(0));
    }

    @Override
    public List<DetallePedidoProgramado> crearDetalles(List<DetallePedidoProgramado> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> bodies = detalles.stream().map(det -> {
            Map<String, Object> body = new HashMap<>();
            body.put("id_pedido_programado", det.getIdPedidoProgramado());
            body.put("id_producto", det.getIdProducto());
            body.put("cantidad", det.getCantidad());
            return body;
        }).toList();

        List<Map<String, Object>> result = supabaseHttpClient.insert(TABLE_DETALLES, bodies, null, "return=representation");
        if (result.size() != detalles.size()) {
            throw new RuntimeException("Error al insertar detalles del pedido programado en Supabase");
        }

        return result.stream().map(this::mapToDetallePedidoProgramado).toList();
    }

    @Override
    public Optional<PedidoProgramado> obtenerPorId(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_pedido_programado", "eq." + id);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_PEDIDOS, queryParams);
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        PedidoProgramado pedido = mapToPedidoProgramado(rows.get(0));
        pedido.setDetalles(obtenerDetallesPorPedidoId(id));
        return Optional.of(pedido);
    }

    @Override
    public List<DetallePedidoProgramado> obtenerDetallesPorPedidoId(String idPedido) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_pedido_programado", "eq." + idPedido);
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_DETALLES, queryParams);
        return rows.stream().map(this::mapToDetallePedidoProgramado).toList();
    }

    @Override
    public List<PedidoProgramado> obtenerTodos() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_PEDIDOS, queryParams);
        return rows.stream().map(row -> {
            PedidoProgramado p = mapToPedidoProgramado(row);
            p.setDetalles(obtenerDetallesPorPedidoId(p.getId()));
            return p;
        }).toList();
    }

    @Override
    public List<PedidoProgramado> obtenerPorVendedorYFecha(String idVendedor, LocalDate fecha) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_vendedor", "eq." + idVendedor);
        queryParams.put("fecha_programada", "eq." + fecha.toString());
        queryParams.put("select", "*");

        List<Map<String, Object>> rows = supabaseHttpClient.select(TABLE_PEDIDOS, queryParams);
        return rows.stream().map(row -> {
            PedidoProgramado p = mapToPedidoProgramado(row);
            p.setDetalles(obtenerDetallesPorPedidoId(p.getId()));
            return p;
        }).toList();
    }

    @Override
    public PedidoProgramado actualizar(PedidoProgramado pedido) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id_pedido_programado", "eq." + pedido.getId());

        Map<String, Object> body = new HashMap<>();
        body.put("id_cliente", pedido.getIdCliente());
        body.put("id_vendedor", pedido.getIdVendedor());
        body.put("fecha_programada", pedido.getFechaProgramada().toString());
        body.put("prioridad", pedido.getPrioridad().name());
        body.put("estado", pedido.getEstado().name());
        body.put("observaciones", pedido.getObservaciones());

        List<Map<String, Object>> result = supabaseHttpClient.update(TABLE_PEDIDOS, queryParams, body);
        if (result.isEmpty()) {
            throw new RuntimeException("Error al actualizar pedido programado en Supabase");
        }

        PedidoProgramado p = mapToPedidoProgramado(result.get(0));
        p.setDetalles(obtenerDetallesPorPedidoId(p.getId()));
        return p;
    }

    // Mapeadores auxiliares
    private PedidoProgramado mapToPedidoProgramado(Map<String, Object> row) {
        String id = getString(row.get("id_pedido_programado"));
        Integer idCliente = getInteger(row.get("id_cliente"));
        String idVendedor = getString(row.get("id_vendedor"));
        
        LocalDate fechaProgramada = null;
        Object fechaObj = row.get("fecha_programada");
        if (fechaObj instanceof String fechaStr) {
            try {
                fechaProgramada = LocalDate.parse(fechaStr);
            } catch (Exception e) {
                // Formato fallback
            }
        }

        PrioridadPedido prioridad = PrioridadPedido.MEDIA;
        Object prioridadObj = row.get("prioridad");
        if (prioridadObj != null) {
            try {
                prioridad = PrioridadPedido.valueOf(getString(prioridadObj).toUpperCase());
            } catch (Exception e) {
                // Fallback a MEDIA
            }
        }

        EstadoPedido estado = EstadoPedido.PENDIENTE;
        Object estadoObj = row.get("estado");
        if (estadoObj != null) {
            try {
                estado = EstadoPedido.valueOf(getString(estadoObj).toUpperCase());
            } catch (Exception e) {
                // Fallback a PENDIENTE
            }
        }

        String observaciones = getString(row.get("observaciones"));

        LocalDateTime createdAt = null;
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof String createdAtStr) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr, ISO_FORMATTER);
            } catch (Exception e) {
                // Formato alternativo si tiene offset
                try {
                    createdAt = OffsetDateTime.parse(createdAtStr).toLocalDateTime();
                } catch (Exception ex) {
                    // Ignorar fallback
                }
            }
        }

        return new PedidoProgramado(id, idCliente, idVendedor, fechaProgramada, estado, prioridad, observaciones, createdAt, new ArrayList<>());
    }

    private DetallePedidoProgramado mapToDetallePedidoProgramado(Map<String, Object> row) {
        String id = getString(row.get("id_detalle"));
        String idPedidoProgramado = getString(row.get("id_pedido_programado"));
        String idProducto = getString(row.get("id_producto"));
        Integer cantidad = getInteger(row.get("cantidad"));

        return new DetallePedidoProgramado(id, idPedidoProgramado, idProducto, cantidad);
    }

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Integer getInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Long l) {
            return l.intValue();
        }
        if (value instanceof Double d) {
            return d.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
class OffsetDateTime {
    static java.time.OffsetDateTime parse(String s) {
        return java.time.OffsetDateTime.parse(s);
    }
}
