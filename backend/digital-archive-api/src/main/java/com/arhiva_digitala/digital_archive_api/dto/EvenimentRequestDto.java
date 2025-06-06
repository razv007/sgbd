package com.arhiva_digitala.digital_archive_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class EvenimentRequestDto {
    private String titlu;
    private String descriere;
    private LocalDateTime dataInceput;
    private LocalDateTime dataSfarsit;
    private String locatie;
    private String categorie;
    private String vizibilitate;
    private List<String> participanti; // <- new
}

