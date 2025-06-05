package com.arhiva_digitala.digital_archive_api.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Prietenii", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"utilizator1_id", "utilizator2_id"})
})
public class Prietenie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_prietenie_generator")
    @SequenceGenerator(name = "seq_prietenie_generator", sequenceName = "seq_prietenie_id", allocationSize = 1)
    @Column(name = "prietenie_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizator1_id", nullable = false)
    private User utilizator1; // Cel care inițiază sau ID-ul mai mic

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizator2_id", nullable = false)
    private User utilizator2; // Cel care primește sau ID-ul mai mare

    @Enumerated(EnumType.STRING)
    @Column(name = "stare_prietenie", nullable = false, length = 20)
    private StarePrietenie stare;

    @Column(name = "data_solicitare", nullable = false)
    private LocalDateTime dataSolicitare;

    @Column(name = "data_raspuns")
    private LocalDateTime dataRaspuns;

    // Constructori
    public Prietenie() {
        this.dataSolicitare = LocalDateTime.now();
        this.stare = StarePrietenie.IN_ASTEPTARE;
    }

    public Prietenie(User sender, User receiver) {
        this();
        this.utilizator1 = sender; // Convenție: utilizator1 este cel care trimite cererea
        this.utilizator2 = receiver; // Convenție: utilizator2 este cel care primește cererea
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUtilizator1() {
        return utilizator1;
    }

    public void setUtilizator1(User utilizator1) {
        this.utilizator1 = utilizator1;
    }

    public User getUtilizator2() {
        return utilizator2;
    }

    public void setUtilizator2(User utilizator2) {
        this.utilizator2 = utilizator2;
    }

    public StarePrietenie getStare() {
        return stare;
    }

    public void setStare(StarePrietenie stare) {
        this.stare = stare;
    }

    public LocalDateTime getDataSolicitare() {
        return dataSolicitare;
    }

    public void setDataSolicitare(LocalDateTime dataSolicitare) {
        this.dataSolicitare = dataSolicitare;
    }

    public LocalDateTime getDataRaspuns() {
        return dataRaspuns;
    }

    public void setDataRaspuns(LocalDateTime dataRaspuns) {
        this.dataRaspuns = dataRaspuns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prietenie prietenie = (Prietenie) o;

        return id != null ? id.equals(prietenie.id) : prietenie.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
