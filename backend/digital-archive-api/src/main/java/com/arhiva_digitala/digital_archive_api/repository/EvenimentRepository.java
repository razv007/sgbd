package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Eveniment;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvenimentRepository extends JpaRepository<Eveniment, Long> {
    List<Eveniment> findByUtilizatorOrderByDataInceputDesc(Utilizator utilizator);
    // Adaugati alte metode de căutare specifice dacă este necesar
    // de exemplu, căutare după titlu, categorie, interval de date etc.
}
