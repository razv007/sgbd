package com.arhiva_digitala.digital_archive_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "utilizatori") // Asigură-te că numele tabelului este corect
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilizator_id")
    private Long id;

    @Column(name = "nume_utilizator", nullable = false, unique = true)
    private String numeUtilizator; // Sau username

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "parola_hash", nullable = false)
    private String parola; // Sau password

    @Column(name = "nume_complet")
    private String numeComplet;

    @Column(name = "data_nastere")
    private LocalDate dataNasterii;

    // Constructori
    public User() {}

    public User(String numeUtilizator, String email, String parola, String numeComplet, LocalDate dataNasterii) {
        this.numeUtilizator = numeUtilizator;
        this.email = email;
        this.parola = parola;
        this.numeComplet = numeComplet;
        this.dataNasterii = dataNasterii;
    }

    // Getters și Setters
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

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public String getNumeComplet() {
        return numeComplet;
    }

    public void setNumeComplet(String numeComplet) {
        this.numeComplet = numeComplet;
    }

    public LocalDate getDataNasterii() {
        return dataNasterii;
    }

    public void setDataNasterii(LocalDate dataNasterii) {
        this.dataNasterii = dataNasterii;
    }
}
