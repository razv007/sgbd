package com.arhiva_digitala.digital_archive_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documente")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_seq")
    @SequenceGenerator(name = "document_seq", sequenceName = "seq_document_id", allocationSize = 1)
    @Column(name = "document_id", nullable = false, unique = true)
    private Long id;


    @Column(nullable = false, columnDefinition = "TEXT", unique = true, name = "url")
    private String url;

    @Column(nullable = false, name = "nume_fisier")
    private String numeFisier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "eveniment_id", nullable = false)
    private Eveniment eveniment;
}
