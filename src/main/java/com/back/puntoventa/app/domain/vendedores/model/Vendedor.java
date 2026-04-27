package com.back.puntoventa.app.domain.vendedores.model;

import java.time.LocalDateTime;

public class Vendedor {

    private final String id;
    private final String nombre;
    private final String email;
    private final String estado;
    private final LocalDateTime createdAt;

    public Vendedor(String id, String nombre, String email, String estado, LocalDateTime createdAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email no puede estar vacío");
        }
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
