package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import necesar
import org.springframework.data.repository.query.Param; // Import necesar
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilizatorRepository extends JpaRepository<Utilizator, Long> {

    Optional<Utilizator> findByNumeUtilizator(String numeUtilizator);

    Optional<Utilizator> findByEmail(String email);

    // Suprascriem interogarea pentru a folosi sintaxa Oracle ROWNUM
    @Query(value = "SELECT CASE WHEN COUNT(u.utilizator_id) > 0 THEN 1 ELSE 0 END FROM Utilizatori u WHERE u.nume_utilizator = :numeUtilizator AND ROWNUM = 1", nativeQuery = true)
    Integer checkExistsByNumeUtilizator(@Param("numeUtilizator") String numeUtilizator);

    // Suprascriem interogarea pentru a folosi sintaxa Oracle ROWNUM
    @Query(value = "SELECT CASE WHEN COUNT(u.utilizator_id) > 0 THEN 1 ELSE 0 END FROM Utilizatori u WHERE u.email = :email AND ROWNUM = 1", nativeQuery = true)
    Integer checkExistsByEmail(@Param("email") String email);
}
