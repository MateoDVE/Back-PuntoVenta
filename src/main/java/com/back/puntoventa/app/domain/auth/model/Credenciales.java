package com.back.puntoventa.app.domain.auth.model;

public class Credenciales {

    private final String email;
    private final String password;

    public Credenciales(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password no puede estar vacío");
        }
        this.email = email.toLowerCase().trim();
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
