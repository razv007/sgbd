package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA va genera automat implementarea pentru această metodă
    // datorită convenției de numire.
    // Caută un utilizator după 'numeUtilizator'. Asigură-te că 'numeUtilizator'
    // este un câmp în entitatea ta User.
    Optional<User> findByNumeUtilizator(String numeUtilizator);

    // Poți adăuga și alte metode de căutare dacă este necesar, de exemplu:
    // Optional<User> findByEmail(String email);
    // Boolean existsByNumeUtilizator(String numeUtilizator);
    // Boolean existsByEmail(String email);
}
