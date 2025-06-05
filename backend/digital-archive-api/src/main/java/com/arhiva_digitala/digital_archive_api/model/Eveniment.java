package com.arhiva_digitala.digital_archive_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "EVENIMENTE")
@Data
@NoArgsConstructor
public class Eveniment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_eveniment_id_generator")
    @SequenceGenerator(name = "seq_eveniment_id_generator", sequenceName = "SEQ_EVENIMENT_ID", allocationSize = 1)
    @Column(name = "EVENIMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UTILIZATOR_ID", nullable = false)
    private Utilizator utilizator;

    @Column(name = "TITLU", nullable = false, length = 200)
    private String titlu;

    @Lob // Pentru descrieri mai lungi
    @Column(name = "DESCRIERE")
    private String descriere;

    @Column(name = "DATA_INCEPUT", nullable = false)
    private LocalDateTime dataInceput;

    @Column(name = "DATA_SFARSIT")
    private LocalDateTime dataSfarsit;

    @Column(name = "LOCATIE", length = 255)
    private String locatie;

    @Column(name = "TIP_EVENIMENT", length = 50) // Mapat la TIP_EVENIMENT din schema.sql
    private String categorie;

    @Column(name = "VIZIBILITATE", nullable = false, length = 20)
    private String vizibilitate = "PRIVAT";

    @Column(name = "DATA_CREARE", nullable = false, updatable = false)
    private LocalDateTime dataCreare;

    @Column(name = "DATA_ULTIMA_MODIFICARE")
    private LocalDateTime dataUltimaModificare;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        dataCreare = now;
        dataUltimaModificare = now;
        if (this.vizibilitate == null) { // Asigură o valoare default dacă nu e setată explicit
            this.vizibilitate = "PRIVAT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataUltimaModificare = LocalDateTime.now();
    }
}
