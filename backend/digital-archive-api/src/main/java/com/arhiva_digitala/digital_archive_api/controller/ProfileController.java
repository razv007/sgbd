package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.ChangePasswordDto;
import com.arhiva_digitala.digital_archive_api.dto.UserProfileDto;
import com.arhiva_digitala.digital_archive_api.model.User;
import com.arhiva_digitala.digital_archive_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/profil")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator neautentificat sau sesiune invalidă.");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Tip de principal neașteptat.");
        }

        try {
            User user = userService.findByNumeUtilizator(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profilul nu a fost găsit pentru utilizatorul: " + username);
            }

            UserProfileDto userProfileDto = new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNumeComplet(),
                user.getDataNasterii()
            );
            return ResponseEntity.ok(userProfileDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare server la preluarea profilului utilizatorului.");
        }
    }

    // Metodă NOUĂ pentru actualizarea profilului
    @PutMapping("/profil")
    public ResponseEntity<?> updateUserProfile(Authentication authentication, @RequestBody UserProfileDto userProfileUpdateDto) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator neautentificat sau sesiune invalidă.");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Tip de principal neașteptat.");
        }

        logger.debug("Attempting to update profile for user '{}' with DTO: {}", username, userProfileUpdateDto);
        try {
            // Aici, userProfileUpdateDto conține noile date. Username-ul este cel autentificat.
            // Este important ca UserProfileDto să aibă un setter pentru dataNasterii care acceptă String
            // sau să gestionăm conversia String -> LocalDate aici sau în service.
            // Pentru moment, presupunem că UserProfileDto poate gestiona corect intrarea.

            User updatedUser = userService.updateUserProfile(username, userProfileUpdateDto);
            
            UserProfileDto responseDto = new UserProfileDto(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getNumeComplet(),
                updatedUser.getDataNasterii()
            );
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e) { // Specific pentru erori de validare, ex. format dată
         logger.warn("IllegalArgumentException while updating profile for user '{}': {}", username, e.getMessage(), e);
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (UsernameNotFoundException e) { // Catch specific for user not found
        logger.warn("UsernameNotFoundException while updating profile for user '{}': {}. This likely means the user was deleted mid-session or an issue with token validation.", username, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) { // Generic catch for other unexpected errors
        logger.error("Unexpected error while updating profile for user '{}': {}", username, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare server la actualizarea profilului utilizatorului: " + e.getMessage());
    }
    }

}