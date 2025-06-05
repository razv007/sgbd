package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Participare;
import com.arhiva_digitala.digital_archive_api.model.ParticipariId;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipareRepository extends JpaRepository<Participare, ParticipariId> {
    @Query("SELECT p FROM Participare p WHERE p.utilizator = :utilizator")
    List<Participare> findByUtilizator(Utilizator utilizator);
}
