package com.back.puntoventa.app.application.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la creación de un pedido programado.
 */
public class CreatePedidoProgramadoDto {

    @NotNull(message = "El idCliente es obligatorio")
    @JsonProperty("id_cliente")
    @JsonAlias("idCliente")
    private Integer idCliente;

    @NotNull(message = "La fecha programada es obligatoria")
    @JsonProperty("fecha_programada")
    @JsonAlias("fechaProgramada")
    private LocalDate fechaProgramada;

    @NotEmpty(message = "Debe registrar al menos un detalle de producto")
    @Valid
    private List<DetallePedidoDto> detalles;

    private String observaciones;

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad;

    public CreatePedidoProgramadoDto() {}

    public CreatePedidoProgramadoDto(Integer idCliente, LocalDate fechaProgramada,
                                      List<DetallePedidoDto> detalles, String observaciones,
                                      String prioridad) {
        this.idCliente = idCliente;
        this.fechaProgramada = fechaProgramada;
        this.detalles = detalles;
        this.observaciones = observaciones;
        this.prioridad = prioridad;
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

    public List<DetallePedidoDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoDto> detalles) {
        this.detalles = detalles;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
