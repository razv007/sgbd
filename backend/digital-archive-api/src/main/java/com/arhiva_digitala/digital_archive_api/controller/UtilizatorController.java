package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.repository.UtilizatorRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilizatori")
@AllArgsConstructor
public class UtilizatorController {
    private final UtilizatorRepository utilizatorRepository;

    @GetMapping("/validate")
    public ResponseEntity<Void> validateUsername(@RequestParam String username) {
        boolean exists = utilizatorRepository.existsByNumeUtilizator(username);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
