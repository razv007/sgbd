package com.arhiva_digitala.digital_archive_api.dto;

public class UserDto {
    private Long id;
    private String numeUtilizator;
    // Alte câmpuri relevante, dacă este necesar (ex: username, email)

    public UserDto() {
    }

    public UserDto(Long id, String numeUtilizator) {
        this.id = id;
        this.numeUtilizator = numeUtilizator;
    }

    // Getters and Setters
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
}
