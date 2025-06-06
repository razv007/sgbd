package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilizator, Long> {
    Optional<Utilizator> findByNumeUtilizator(String numeUtilizator);

    @Query(value = "SELECT FN_CALC_USER_SIMILARITY(:userId1, :userId2) FROM dual", nativeQuery = true)
    Double calculateSimilarityScore(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    // Optional<Utilizator> findByEmail(String email); // Exemplu de altă metodă utilă
    // Boolean existsByNumeUtilizator(String numeUtilizator);
    // Boolean existsByEmail(String email);
}
