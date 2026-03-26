package com.back.puntoventa.app.domain.productos.model;

import java.time.LocalDateTime;

/**
 * Entidad del dominio: Producto.
 * Inmutable - representa un producto del catálogo.
 */
public final class Producto {
    private final String id;
    private final String sku;
    private final Integer idCategoria;
    private final String nombre;
    private final Double precioUnidad;
    private final Double precioCaja;
    private final Integer unidadesPorCaja;
    private final Integer stockAlmacenCentral;
    private final String descripcion;
    private final String urlImagen;
    private final String estado;
    private final LocalDateTime createdAt;

    public Producto(String id, String sku, Integer idCategoria, String nombre, Double precioUnidad,
            Double precioCaja, Integer unidadesPorCaja, Integer stockAlmacenCentral,
            String descripcion, String urlImagen, String estado, LocalDateTime createdAt) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre del producto no puede estar vacío");
        }
        if (precioUnidad == null || precioUnidad <= 0) {
            throw new IllegalArgumentException("Precio unidad debe ser mayor a 0");
        }

        this.id = id;
        this.sku = sku;
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.precioUnidad = precioUnidad;
        this.precioCaja = precioCaja;
        this.unidadesPorCaja = unidadesPorCaja;
        this.stockAlmacenCentral = stockAlmacenCentral;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getPrecioUnidad() {
        return precioUnidad;
    }

    public Double getPrecioCaja() {
        return precioCaja;
    }

    public Integer getUnidadesPorCaja() {
        return unidadesPorCaja;
    }

    public Integer getStockAlmacenCentral() {
        return stockAlmacenCentral;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
