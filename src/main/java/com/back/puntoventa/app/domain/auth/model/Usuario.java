package com.back.puntoventa.app.domain.auth.model;

import java.time.LocalDateTime;

public class Usuario {

    private final String id;
    private final String nombre;
    private final String email;
    private final String rol;
    private final String estado;
    private final LocalDateTime createdAt;

    public Usuario(String id, String nombre, String email, String rol, String estado, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
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

    public String getRol() {
        return rol;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
