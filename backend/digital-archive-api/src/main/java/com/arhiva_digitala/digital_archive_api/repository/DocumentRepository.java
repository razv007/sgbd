package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Document;
import com.arhiva_digitala.digital_archive_api.model.Eveniment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.url = :url")
    boolean existsByUrl(@Param("url") String url);

    @Query("SELECT d FROM Document d WHERE d.eveniment = :eveniment")
    List<Document> findAllByEveniment(@Param("eveniment") Eveniment eveniment);
}
