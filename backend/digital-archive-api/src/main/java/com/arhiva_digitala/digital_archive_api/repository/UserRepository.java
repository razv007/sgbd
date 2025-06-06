package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilizator, Long> {
    Optional<Utilizator> findByNumeUtilizator(String numeUtilizator);
    // Optional<Utilizator> findByEmail(String email); // Exemplu de altă metodă utilă
    // Boolean existsByNumeUtilizator(String numeUtilizator);
    // Boolean existsByEmail(String email);
}
