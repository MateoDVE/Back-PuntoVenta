package com.back.puntoventa.app.application.vendedores.dto;

import jakarta.validation.constraints.Pattern;

public class CreateVendedorDto {
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚáéíóúÑñ]).+$", message = "El nombre del vendedor debe contener letras y no puede ser puramente numérico")
    private String nombre;
    private String email;
    private String password;

    public CreateVendedorDto() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
