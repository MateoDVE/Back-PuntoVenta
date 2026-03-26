package com.back.puntoventa.app.application.productos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateProductoDto {
    private String sku;
    @JsonProperty("id_categoria")
    @JsonAlias("idCategoria")
    private Integer idCategoria;
    private String nombre;
    private String descripcion;
    @JsonProperty("url_imagen")
    @JsonAlias("urlImagen")
    private String urlImagen;
    @JsonProperty("precio_unidad")
    @JsonAlias("precioUnidad")
    private Double precioUnidad;
    @JsonProperty("precio_caja")
    @JsonAlias("precioCaja")
    private Double precioCaja;
    @JsonProperty("unidades_por_caja")
    @JsonAlias("unidadesPorCaja")
    private Integer unidadesPorCaja;
    @JsonProperty("stock_almacen_central")
    @JsonAlias("stockAlmacenCentral")
    private Integer stockAlmacenCentral;

    public CreateProductoDto() {}

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Double getPrecioUnidad() {
        return precioUnidad;
    }

    public void setPrecioUnidad(Double precioUnidad) {
        this.precioUnidad = precioUnidad;
    }

    public Double getPrecioCaja() {
        return precioCaja;
    }

    public void setPrecioCaja(Double precioCaja) {
        this.precioCaja = precioCaja;
    }

    public Integer getUnidadesPorCaja() {
        return unidadesPorCaja;
    }

    public void setUnidadesPorCaja(Integer unidadesPorCaja) {
        this.unidadesPorCaja = unidadesPorCaja;
    }

    public Integer getStockAlmacenCentral() {
        return stockAlmacenCentral;
    }

    public void setStockAlmacenCentral(Integer stockAlmacenCentral) {
        this.stockAlmacenCentral = stockAlmacenCentral;
    }
}
