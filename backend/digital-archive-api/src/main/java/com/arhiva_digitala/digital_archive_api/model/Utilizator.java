package com.arhiva_digitala.digital_archive_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "UTILIZATORI")
public class Utilizator {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "utilizator_seq_gen")
    @SequenceGenerator(name = "utilizator_seq_gen", sequenceName = "SEQ_UTILIZATOR_ID", allocationSize = 1)
    @Column(name = "UTILIZATOR_ID")
    private Long id;

    @Column(name = "NUME_UTILIZATOR", unique = true, nullable = false, length = 50)
    private String numeUtilizator;

    @Column(name = "PAROLA_HASH", nullable = false, length = 255) // Matches schema.sql
    private String parola;

    @Column(name = "EMAIL", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "NUME_COMPLET", length = 100)
    private String numeComplet;

    @Column(name = "DATA_INREGISTRARE", nullable = false, updatable = false)
    private LocalDateTime dataInregistrare;

    @Column(name = "DATA_NASTERE")
    private LocalDate dataNastere;

    @Column(name = "ULTIMA_LOGARE")
    private LocalDateTime ultimaLogare;

    @PrePersist
    protected void onCreate() {
        if (this.dataInregistrare == null) {
            this.dataInregistrare = LocalDateTime.now();
        }
    }
}
