package com.arhiva_digitala.digital_archive_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenimentResponseDTO {
    private Long id;
    private String titlu;
    private String descriere;
    private LocalDateTime dataInceput;
    private LocalDateTime dataSfarsit;
    private String locatie;
    private String categorie;
    private String vizibilitate;
    private LocalDateTime dataCreare;
    private LocalDateTime dataUltimaModificare;
    private String numeUtilizator; // 👈 owner username
}
