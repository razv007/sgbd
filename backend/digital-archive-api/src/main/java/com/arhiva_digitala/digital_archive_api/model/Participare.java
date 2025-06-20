package com.arhiva_digitala.digital_archive_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PARTICIPARI")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participare {

    @EmbeddedId
    private ParticipariId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("evenimentId")
    @JoinColumn(name = "EVENIMENT_ID", nullable = false)
    @JsonIgnore
    private Eveniment eveniment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilizatorId")
    @JoinColumn(name = "UTILIZATOR_ID", nullable = false)
    @JsonIgnore
    private Utilizator utilizator;
}