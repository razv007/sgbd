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

    @Query(value = "SELECT FN_CALC_USER_SIMILARITY(:username1, :username2) FROM dual", nativeQuery = true)
    Double calculateSimilarityScore(@Param("username1") String username1, @Param("username2") String username2);
}
