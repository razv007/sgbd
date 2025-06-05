package com.arhiva_digitala.digital_archive_api.dto;

import com.arhiva_digitala.digital_archive_api.model.Document;

public record DocumentDTO(Long id, String name, String url) {
    public DocumentDTO(Document doc) {
        this(doc.getId(), doc.getNumeFisier(), doc.getUrl());
    }
}
