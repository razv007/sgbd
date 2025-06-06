package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.model.*;
import com.arhiva_digitala.digital_archive_api.repository.DocumentRepository;
import com.arhiva_digitala.digital_archive_api.repository.EvenimentRepository;
import com.arhiva_digitala.digital_archive_api.repository.ParticipareRepository;
import com.arhiva_digitala.digital_archive_api.repository.UtilizatorRepository;
// Ar putea fi necesare excepții personalizate, de ex. ResourceNotFoundException
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Sau o excepție personalizată
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class EvenimentService {

    private final EvenimentRepository evenimentRepository;
    private final UtilizatorRepository utilizatorRepository;
    private final ParticipareRepository participareRepository;
    private final DocumentRepository documentRepository;
    private final S3Service s3Service;

    @Transactional
    public Eveniment createEveniment(Eveniment eveniment, String creatorUsername, List<String> participanti) {
        Utilizator creator = utilizatorRepository.findByNumeUtilizator(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + creatorUsername));

        eveniment.setUtilizator(creator);
        Eveniment saved = evenimentRepository.save(eveniment);

        // Add creator as participant
        Participare self = new Participare(new ParticipariId(saved.getId(), creator.getId()), saved, creator);
        participareRepository.save(self);

        // Add other participants
        if (participanti != null) {
            for (String username : participanti) {
                if (!username.equals(creatorUsername)) {
                    utilizatorRepository.findByNumeUtilizator(username).ifPresent(user -> {
                        Participare p = new Participare(new ParticipariId(saved.getId(), user.getId()), saved, user);
                        participareRepository.save(p);
                    });
                }
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Eveniment> getEvenimenteVizibilePentruUtilizator(String timelineOwnerUsername, String requesterUsername) {
        Utilizator timelineOwner = utilizatorRepository.findByNumeUtilizator(timelineOwnerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + timelineOwnerUsername));

        Utilizator requester = utilizatorRepository.findByNumeUtilizator(requesterUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + requesterUsername));

        List<Participare> participari = participareRepository.findByUtilizator(timelineOwner);
        return participari.stream()
                .map(Participare::getEveniment)
                .filter(e -> e.getVizibilitate().equals("PUBLIC") ||
                        e.getParticipanti().stream().anyMatch(p -> p.getUtilizator().equals(requester)))
                .sorted((a, b) -> b.getDataInceput().compareTo(a.getDataInceput()))
                .toList();
    }



    @Transactional(readOnly = true)
    public List<Eveniment> getEvenimenteByUtilizator(String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));

        List<Participare> participari = participareRepository.findByUtilizator(utilizator);
        return participari.stream()
                .map(Participare::getEveniment)
                .sorted((a, b) -> b.getDataInceput().compareTo(a.getDataInceput()))
                .toList();
    }


    @Transactional(readOnly = true)
    public Eveniment getEvenimentByIdAndUtilizator(Long id, String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));
        return evenimentRepository.findById(id)
                .filter(event -> event.getUtilizator().equals(utilizator))
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost găsit sau nu aparține utilizatorului: " + id)); // TODO: Custom exception
    }

    @Transactional
    public Eveniment updateEveniment(Long id, Eveniment evenimentDetails, String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));

        Eveniment evenimentExistent = evenimentRepository.findById(id)
                .filter(event -> event.getUtilizator().equals(utilizator))
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost găsit sau nu poate fi actualizat: " + id)); // TODO: Custom exception

        evenimentExistent.setTitlu(evenimentDetails.getTitlu());
        evenimentExistent.setDescriere(evenimentDetails.getDescriere());
        evenimentExistent.setDataInceput(evenimentDetails.getDataInceput());
        evenimentExistent.setDataSfarsit(evenimentDetails.getDataSfarsit());
        evenimentExistent.setLocatie(evenimentDetails.getLocatie());
        evenimentExistent.setCategorie(evenimentDetails.getCategorie());
        evenimentExistent.setVizibilitate(evenimentDetails.getVizibilitate());

        return evenimentRepository.save(evenimentExistent);
    }

    @Transactional
    public void deleteEveniment(Long eventId, String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));

        System.out.println("Deleting eventId: " + eventId + " for userId: " + utilizator.getId());

        Participare participare = participareRepository.findByEvenimentIdAndUtilizatorId(eventId, utilizator.getId())
                .orElseThrow(() -> new RuntimeException("Participarea nu a fost găsită."));

        participareRepository.delete(participare);

        boolean hasOtherParticipants = participareRepository.existsByEvenimentId(eventId);

        if (!hasOtherParticipants) {
            // delete documents from S3 + DB
            List<Document> docs = documentRepository.findByEvenimentId(eventId);
            for (Document doc : docs) {
                s3Service.deleteFile(doc.getUrl());
            }
            documentRepository.deleteAll(docs);

            // delete the event itself
            evenimentRepository.deleteById(eventId);
        }
    }

}
