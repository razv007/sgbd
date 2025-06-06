package com.arhiva_digitala.digital_archive_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserDto {

    @NotBlank(message = "Emailul nu poate fi gol.")
    @Email(message = "Formatul emailului este invalid.")
    @Size(max = 100, message = "Emailul nu poate depăși 100 de caractere.")
    private String email;

    @NotBlank(message = "Numele complet nu poate fi gol.")
    @Size(min = 3, max = 100, message = "Numele complet trebuie să aibă între 3 și 100 de caractere.")
    private String numeComplet;

    // Permite format YYYY-MM-DD. Poate fi null dacă utilizatorul nu dorește să-l seteze/actualizeze.
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Data nașterii trebuie să fie în formatul YYYY-MM-DD.")
    private String dataNastere; // Vom parsa acest string în LocalDate în serviciu

    public UpdateUserDto() {
    }

    public UpdateUserDto(String email, String numeComplet, String dataNastere) {
        this.email = email;
        this.numeComplet = numeComplet;
        this.dataNastere = dataNastere;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumeComplet() {
        return numeComplet;
    }

    public void setNumeComplet(String numeComplet) {
        this.numeComplet = numeComplet;
    }

    public String getDataNastere() {
        return dataNastere;
    }

    public void setDataNastere(String dataNastere) {
        this.dataNastere = dataNastere;
    }
}
