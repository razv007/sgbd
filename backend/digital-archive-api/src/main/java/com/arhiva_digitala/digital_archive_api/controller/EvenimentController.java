package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.EvenimentRequestDto;
import com.arhiva_digitala.digital_archive_api.model.Eveniment;
import com.arhiva_digitala.digital_archive_api.service.EvenimentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evenimente")
public class EvenimentController {

    private final EvenimentService evenimentService;

    @Autowired
    public EvenimentController(EvenimentService evenimentService) {
        this.evenimentService = evenimentService;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Utilizatorul nu este autentificat.");
        }
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<Eveniment> createEveniment(@Valid @RequestBody EvenimentRequestDto evenimentDto) {
        String username = getCurrentUsername();
        Eveniment evenimentToCreate = new Eveniment();
        // Map DTO to Entity
        evenimentToCreate.setTitlu(evenimentDto.getTitlu());
        evenimentToCreate.setDescriere(evenimentDto.getDescriere());
        evenimentToCreate.setDataInceput(evenimentDto.getDataInceput());
        evenimentToCreate.setDataSfarsit(evenimentDto.getDataSfarsit());
        evenimentToCreate.setLocatie(evenimentDto.getLocatie());
        evenimentToCreate.setCategorie(evenimentDto.getCategorie());
        evenimentToCreate.setVizibilitate(evenimentDto.getVizibilitate());
        // Utilizatorul va fi setat în serviciu

        Eveniment createdEveniment = evenimentService.createEveniment(evenimentToCreate, username);
        return new ResponseEntity<>(createdEveniment, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Eveniment>> getEvenimenteUtilizator() {
        String username = getCurrentUsername();
        List<Eveniment> evenimente = evenimentService.getEvenimenteByUtilizator(username);
        return ResponseEntity.ok(evenimente);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Eveniment> getEvenimentById(@PathVariable Long id) {
        String username = getCurrentUsername();
        try {
            Eveniment eveniment = evenimentService.getEvenimentByIdAndUtilizator(id, username);
            return ResponseEntity.ok(eveniment);
        } catch (RuntimeException e) { // TODO: Specific exception handling for not found vs not authorized
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Sau FORBIDDEN
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Eveniment> updateEveniment(@PathVariable Long id, @Valid @RequestBody EvenimentRequestDto evenimentDto) {
        String username = getCurrentUsername();
        Eveniment evenimentDetails = new Eveniment();
        // Map DTO to Entity
        evenimentDetails.setTitlu(evenimentDto.getTitlu());
        evenimentDetails.setDescriere(evenimentDto.getDescriere());
        evenimentDetails.setDataInceput(evenimentDto.getDataInceput());
        evenimentDetails.setDataSfarsit(evenimentDto.getDataSfarsit());
        evenimentDetails.setLocatie(evenimentDto.getLocatie());
        evenimentDetails.setCategorie(evenimentDto.getCategorie());
        evenimentDetails.setVizibilitate(evenimentDto.getVizibilitate());

        try {
            Eveniment updatedEveniment = evenimentService.updateEveniment(id, evenimentDetails, username);
            return ResponseEntity.ok(updatedEveniment);
        } catch (RuntimeException e) { // TODO: Specific exception handling
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Sau FORBIDDEN
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEveniment(@PathVariable Long id) {
        String username = getCurrentUsername();
        try {
            evenimentService.deleteEveniment(id, username);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // TODO: Specific exception handling
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Sau FORBIDDEN
        }
    }
}
