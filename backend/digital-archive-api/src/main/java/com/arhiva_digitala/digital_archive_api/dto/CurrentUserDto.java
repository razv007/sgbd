package com.arhiva_digitala.digital_archive_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDto {
    private Long id;
    private String numeUtilizator;
    private String email;
    private String numeComplet;
    private String dataNastere; // Adăugat pentru data nașterii
    private List<String> roles; // Sau autoritățile, dacă sunt necesare pe frontend
}
