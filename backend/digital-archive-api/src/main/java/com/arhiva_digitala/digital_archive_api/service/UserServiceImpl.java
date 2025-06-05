package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.model.User;
import com.arhiva_digitala.digital_archive_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.arhiva_digitala.digital_archive_api.dto.UserProfileDto;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // ex: "2011-12-03"

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByNumeUtilizator(String numeUtilizator) {
        return userRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit cu numele: " + numeUtilizator));
    }

    @Override
    @Transactional // Este o bună practică să marcăm metodele de service care modifică datele ca tranzacționale
    public User updateUserProfile(String numeUtilizator, UserProfileDto userProfileDto) {
        logger.debug("Început updateUserProfile pentru utilizator: {}, DTO: {}", numeUtilizator, userProfileDto); // Asigură-te că UserProfileDto are un toString() bun
        User userToUpdate = userRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> {
                    logger.warn("Utilizator {} nu a fost găsit pentru actualizare.", numeUtilizator);
                    return new UsernameNotFoundException("Utilizatorul nu a fost găsit...");
                });
    
        logger.debug("Utilizator găsit (înainte de actualizare): {}", userToUpdate); // Asigură-te că User are un toString() bun
    
        boolean isModified = false; // Flag pentru a vedea dacă s-a modificat ceva

        // Actualizare email
        if (userProfileDto.getEmail() != null && !userProfileDto.getEmail().trim().isEmpty() && !userProfileDto.getEmail().equals(userToUpdate.getEmail())) {
            logger.debug("Actualizare email de la '{}' la '{}'", userToUpdate.getEmail(), userProfileDto.getEmail().trim());
            userToUpdate.setEmail(userProfileDto.getEmail().trim());
            isModified = true;
        }

        // Actualizare numeComplet
        if (userProfileDto.getNumeComplet() != null && !userProfileDto.getNumeComplet().trim().isEmpty() && !userProfileDto.getNumeComplet().equals(userToUpdate.getNumeComplet())) {
            logger.debug("Actualizare numeComplet de la '{}' la '{}'", userToUpdate.getNumeComplet(), userProfileDto.getNumeComplet());
            userToUpdate.setNumeComplet(userProfileDto.getNumeComplet());
            isModified = true;
        }

        // Gestionăm actualizarea datei nașterii
        Object dataNasteriiObj = userProfileDto.getDataNasterii(); // Obținem obiectul datei
        LocalDate newDataNasterii = null;

        if (dataNasteriiObj != null) {
            try {
                if (dataNasteriiObj instanceof LocalDate) {
                    newDataNasterii = (LocalDate) dataNasteriiObj;
                } else if (dataNasteriiObj instanceof String) {
                    String dataNasteriiStr = (String) dataNasteriiObj;
                    if (!dataNasteriiStr.trim().isEmpty()) {
                        newDataNasterii = LocalDate.parse(dataNasteriiStr.trim(), DATE_FORMATTER);
                    } // Dacă stringul e gol, newDataNasterii rămâne null, permițând ștergerea datei
                } else {
                    logger.warn("Tip de dată neașteptat pentru dataNașterii: {}. Se va ignora.", dataNasteriiObj.getClass().getName());
                }
            } catch (DateTimeParseException e) {
                logger.error("Formatul datei nașterii este invalid. Folosiți formatul YYYY-MM-DD. Valoare primită: '{}'", dataNasteriiObj, e);
                throw new IllegalArgumentException("Formatul datei nașterii este invalid. Folosiți formatul YYYY-MM-DD. Valoare primită: '" + dataNasteriiObj + "'", e);
            }
        }

        // Comparație și actualizare pentru dataNasterii
        // Verificăm dacă (data existentă e diferită de noua dată) SAU (data existentă era setată și acum se dorește null)
        if ((userToUpdate.getDataNasterii() == null && newDataNasterii != null) || 
            (userToUpdate.getDataNasterii() != null && !userToUpdate.getDataNasterii().equals(newDataNasterii)) ||
            (userToUpdate.getDataNasterii() != null && newDataNasterii == null) ) {
            logger.debug("Actualizare dataNasterii de la '{}' la '{}'", userToUpdate.getDataNasterii(), newDataNasterii);
            userToUpdate.setDataNasterii(newDataNasterii);
            isModified = true;
        }
    
        if (isModified) {
            logger.debug("Utilizatorul A FOST MODIFICAT. Se încearcă salvarea. Stare înainte de save: {}", userToUpdate);
        } else {
            logger.debug("Utilizatorul NU A FOST MODIFICAT. Nu se va efectua un save inutil. Stare: {}", userToUpdate);
            // Poți alege să returnezi userToUpdate direct aici dacă nu s-a modificat nimic,
            // sau să lași save() să fie apelat (Hibernate ar trebui să fie suficient de inteligent să nu facă UPDATE dacă nu e nevoie)
        }
        
        User savedUser = userRepository.save(userToUpdate);
        userRepository.flush(); // Forțăm sincronizarea cu BD
        logger.debug("Utilizator DUPĂ save și flush: {}", savedUser);
        return savedUser;
    }

}
