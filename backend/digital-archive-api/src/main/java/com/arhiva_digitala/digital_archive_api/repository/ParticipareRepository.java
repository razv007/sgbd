package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Participare;
import com.arhiva_digitala.digital_archive_api.model.ParticipariId;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface ParticipareRepository extends JpaRepository<Participare, ParticipariId> {
    @Query("SELECT p FROM Participare p WHERE p.utilizator = :utilizator")
    List<Participare> findByUtilizator(Utilizator utilizator);

    @Query("SELECT COUNT(p) > 0 FROM Participare p WHERE p.eveniment.id = :eventId")
    boolean existsByEvenimentId(@Param("eventId") Long eventId);


    @Query("SELECT p FROM Participare p WHERE p.eveniment.id = :eventId AND p.utilizator.id = :userId")
    Optional<Participare> findByEvenimentIdAndUtilizatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);

}
