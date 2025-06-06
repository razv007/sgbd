package com.arhiva_digitala.digital_archive_api.model;

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
    private Long evenimentId;
    private Long utilizatorId;
}