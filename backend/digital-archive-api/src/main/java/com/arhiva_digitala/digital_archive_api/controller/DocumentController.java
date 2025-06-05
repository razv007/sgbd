    package com.arhiva_digitala.digital_archive_api.controller;

    import com.arhiva_digitala.digital_archive_api.dto.DocumentDTO;
    import com.arhiva_digitala.digital_archive_api.model.Document;
    import com.arhiva_digitala.digital_archive_api.model.Eveniment;
    import com.arhiva_digitala.digital_archive_api.repository.DocumentRepository;
    import com.arhiva_digitala.digital_archive_api.repository.EvenimentRepository;
    import com.arhiva_digitala.digital_archive_api.service.S3Service;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.List;

    @RestController
    @RequestMapping("/api/documents")
    @RequiredArgsConstructor
    public class DocumentController {

        private final S3Service s3Service;
        private final DocumentRepository documentRepository;
        private final EvenimentRepository evenimentRepository;

        @PostMapping("/upload")
        public ResponseEntity<DocumentDTO> uploadDocument(
                @RequestParam("eventId") Long eventId,
                @RequestParam("file") MultipartFile file
        ) {
            Eveniment eveniment = evenimentRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            String url = s3Service.uploadFile(file);

            if (documentRepository.existsByUrl(url)) {
                return ResponseEntity.status(409).build();
            }

            Document document = new Document();
            document.setUrl(url);
            document.setNumeFisier(file.getOriginalFilename());
            document.setEveniment(eveniment);

            documentRepository.save(document);

            return ResponseEntity.ok(new DocumentDTO(document));
        }

        @GetMapping
        public ResponseEntity<List<DocumentDTO>> getDocumentsByEvent(@RequestParam("eventId") Long eventId) {
            Eveniment eveniment = evenimentRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            var documents = documentRepository.findAllByEveniment(eveniment)
                    .stream()
                    .map(DocumentDTO::new)
                    .toList();

            return ResponseEntity.ok(documents);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // 1. Delete from S3
            s3Service.deleteFile(document.getUrl());

            // 2. Delete from DB
            documentRepository.delete(document);

            return ResponseEntity.noContent().build();
        }


    }
