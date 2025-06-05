package com.arhiva_digitala.digital_archive_api.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserProfileDto {
    private Long id;
    private String email;
    private String numeComplet;
    private String dataNasterii;

    public UserProfileDto() {}

    public UserProfileDto(Long id, String email, String numeComplet, LocalDate dataNasteriiDate) {
        this.id = id;
        this.email = email;
        this.numeComplet = numeComplet;
        this.dataNasterii = dataNasteriiDate != null ? dataNasteriiDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public String getNumeComplet() { return numeComplet; }
    public String getDataNasterii() { return dataNasterii; }

    public void setId(Long id) { this.id = id; }

    public void setEmail(String email) { this.email = email; }
    public void setNumeComplet(String numeComplet) { this.numeComplet = numeComplet; }
    public void setDataNasterii(String dataNasterii) { this.dataNasterii = dataNasterii; }
}