package com.back.puntoventa.app.domain.ventas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad del dominio: Venta.
 * Representa una venta realizada.
 */
public final class Venta {
    private final String idVenta;
    private final Integer idCliente;
    private final UUID idVendedor;
    private final LocalDateTime fechaHora;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final BigDecimal totalEfectivo;
    private final String estado;
    private String idTransaccionLocal;

    public Venta(String idVenta, Integer idCliente, UUID idVendedor, LocalDateTime fechaHora,
            BigDecimal subtotal, BigDecimal descuento, BigDecimal totalEfectivo, String estado, String idTransaccionLocal) {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("ID cliente debe ser válido");
        }
        if (idVendedor == null) {
            throw new IllegalArgumentException("ID vendedor no puede ser nulo");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal debe ser mayor o igual a 0");
        }
        if (descuento == null || descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Descuento debe ser mayor o igual a 0");
        }
        if (totalEfectivo == null || totalEfectivo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total efectivo debe ser mayor o igual a 0");
        }

        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idVendedor = idVendedor;
        this.fechaHora = fechaHora;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalEfectivo = totalEfectivo;
        this.estado = estado;
        this.idTransaccionLocal = idTransaccionLocal;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public UUID getIdVendedor() {
        return idVendedor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public String getEstado() {
        return estado;
    }
    public String getIdTransaccionLocal() {
        return idTransaccionLocal;
    }
}