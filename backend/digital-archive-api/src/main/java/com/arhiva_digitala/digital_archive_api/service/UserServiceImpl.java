package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.dto.UpdateUserDto;
import com.arhiva_digitala.digital_archive_api.exception.ResourceNotFoundException;
import com.arhiva_digitala.digital_archive_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Utilizator> findByNumeUtilizator(String numeUtilizator) {
        return userRepository.findByNumeUtilizator(numeUtilizator);
    }

    @Override
    @Transactional
    public Utilizator updateUserProfile(Long userId, UpdateUserDto updateUserDto) {
        Utilizator utilizator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator", "id", userId));

        // Actualizează email dacă este furnizat și diferit
        if (StringUtils.hasText(updateUserDto.getEmail()) && !updateUserDto.getEmail().equals(utilizator.getEmail())) {
            // Aici s-ar putea adăuga logica de verificare a unicității emailului dacă este necesar
            utilizator.setEmail(updateUserDto.getEmail());
        }

        // Actualizează numeComplet dacă este furnizat și diferit
        if (StringUtils.hasText(updateUserDto.getNumeComplet()) && !updateUserDto.getNumeComplet().equals(utilizator.getNumeComplet())) {
            utilizator.setNumeComplet(updateUserDto.getNumeComplet());
        }

        // Actualizează dataNastere dacă este furnizată
        if (StringUtils.hasText(updateUserDto.getDataNastere())) {
            try {
                LocalDate dataNastere = LocalDate.parse(updateUserDto.getDataNastere(), DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
                utilizator.setDataNastere(dataNastere);
            } catch (DateTimeParseException e) {
                // Gestionează eroarea de parsare, de ex. aruncă o excepție specifică sau loghează
                // Pentru moment, vom ignora data invalidă sau vom arunca o excepție generală
                // Considerați adăugarea unei validări mai bune sau a unui feedback specific
                throw new IllegalArgumentException("Formatul datei de naștere este invalid: " + updateUserDto.getDataNastere() + ". Folosiți YYYY-MM-DD.", e);
            }
        } else {
            // Dacă stringul este gol sau null, se poate seta dataNastere la null (dacă modelul permite)
             utilizator.setDataNastere(null);
        }

        return userRepository.save(utilizator);
    }
}
