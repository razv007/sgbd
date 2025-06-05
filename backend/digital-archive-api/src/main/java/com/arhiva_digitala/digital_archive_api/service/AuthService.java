package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.repository.UtilizatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service // Marks this class as a Spring service component
public class AuthService {

    private final UtilizatorRepository utilizatorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired // Constructor injection for dependencies
    public AuthService(UtilizatorRepository utilizatorRepository, PasswordEncoder passwordEncoder) {
        this.utilizatorRepository = utilizatorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilizator registerUser(Utilizator utilizator) throws Exception {
        // Check if username already exists
        if (utilizatorRepository.checkExistsByNumeUtilizator(utilizator.getNumeUtilizator()) == 1) {
            throw new Exception("Username " + utilizator.getNumeUtilizator() + " already exists!");
        }

        // Check if email already exists
        if (utilizatorRepository.checkExistsByEmail(utilizator.getEmail()) == 1) {
            throw new Exception("Email " + utilizator.getEmail() + " is already registered!");
        }

        // Hash the password before saving
        utilizator.setParola(passwordEncoder.encode(utilizator.getParola()));

        // The @PrePersist method in Utilizator entity will set dataInregistrare
        return utilizatorRepository.save(utilizator);
    }

    // We will add login logic here later

    public Utilizator getUtilizatorByNumeUtilizator(String numeUtilizator) {
        Optional<Utilizator> utilizatorOptional = utilizatorRepository.findByNumeUtilizator(numeUtilizator);
        return utilizatorOptional.orElseThrow(() -> new RuntimeException("Utilizator nu a fost găsit cu numele: " + numeUtilizator));
    }
}
