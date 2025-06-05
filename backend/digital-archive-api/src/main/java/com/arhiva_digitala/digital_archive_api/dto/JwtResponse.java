package com.arhiva_digitala.digital_archive_api.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String numeUtilizator;
    private String email;
    private String rol;

    public JwtResponse(String accessToken, Long id, String numeUtilizator, String email, String rol) {
        this.token = accessToken;
        this.id = id;
        this.numeUtilizator = numeUtilizator;
        this.email = email;
        this.rol = rol;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeUtilizator() {
        return numeUtilizator;
    }

    public void setNumeUtilizator(String numeUtilizator) {
        this.numeUtilizator = numeUtilizator;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
