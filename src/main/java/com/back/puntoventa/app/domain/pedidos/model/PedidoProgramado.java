package com.back.puntoventa.app.domain.pedidos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio pura que representa un Pedido Programado.
 */
public class PedidoProgramado {
    private String id;
    private Integer idCliente;
    private LocalDate fechaProgramada;
    private EstadoPedido estado;
    private PrioridadPedido prioridad;
    private String observaciones;
    private LocalDateTime createdAt;
    private List<DetallePedidoProgramado> detalles;

    public PedidoProgramado() {
        this.detalles = new ArrayList<>();
    }

    public PedidoProgramado(String id, Integer idCliente, LocalDate fechaProgramada, EstadoPedido estado,
                            PrioridadPedido prioridad, String observaciones, LocalDateTime createdAt,
                            List<DetallePedidoProgramado> detalles) {
        this.id = id;
        this.idCliente = idCliente;
        this.fechaProgramada = fechaProgramada;
        this.estado = estado;
        this.prioridad = prioridad;
        this.observaciones = observaciones;
        this.createdAt = createdAt;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public PrioridadPedido getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadPedido prioridad) {
        this.prioridad = prioridad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DetallePedidoProgramado> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoProgramado> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
