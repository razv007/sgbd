package com.arhiva_digitala.digital_archive_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class EvenimentRequestDto {

    @NotBlank(message = "Titlul este obligatoriu.")
    @Size(max = 200, message = "Titlul nu poate depăși 200 de caractere.")
    private String titlu;

    private String descriere; // Poate fi null, validare specifică dacă e necesar

    @NotNull(message = "Data de început este obligatorie.")
    private LocalDateTime dataInceput;

    private LocalDateTime dataSfarsit; // Poate fi null

    @Size(max = 255, message = "Locația nu poate depăși 255 de caractere.")
    private String locatie; // Poate fi null

    @Size(max = 50, message = "Categoria (Tip Eveniment) nu poate depăși 50 de caractere.")
    private String categorie; // Poate fi null

    @NotBlank(message = "Vizibilitatea este obligatorie.")
    @Pattern(regexp = "^(PRIVAT|PRIETENI|PUBLIC)$", message = "Vizibilitatea trebuie să fie una dintre: PRIVAT, PRIETENI, PUBLIC.")
    private String vizibilitate = "PRIVAT";
}
