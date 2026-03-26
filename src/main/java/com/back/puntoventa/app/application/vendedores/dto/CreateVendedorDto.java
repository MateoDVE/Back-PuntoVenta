package com.back.puntoventa.app.application.vendedores.dto;

public class CreateVendedorDto {
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
