package com.arhiva_digitala.digital_archive_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ParticipariId implements Serializable {
    @Column(name = "EVENIMENT_ID")
    private Long evenimentId;

    @Column(name = "UTILIZATOR_ID")
    private Long utilizatorId;
}