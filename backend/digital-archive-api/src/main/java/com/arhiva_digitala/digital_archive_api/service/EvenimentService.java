package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.model.Eveniment;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.repository.EvenimentRepository;
import com.arhiva_digitala.digital_archive_api.repository.UtilizatorRepository;
// Ar putea fi necesare excepții personalizate, de ex. ResourceNotFoundException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Sau o excepție personalizată
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvenimentService {

    private final EvenimentRepository evenimentRepository;
    private final UtilizatorRepository utilizatorRepository;

    @Autowired
    public EvenimentService(EvenimentRepository evenimentRepository, UtilizatorRepository utilizatorRepository) {
        this.evenimentRepository = evenimentRepository;
        this.utilizatorRepository = utilizatorRepository;
    }

    @Transactional
    public Eveniment createEveniment(Eveniment eveniment, String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));
        eveniment.setUtilizator(utilizator);
        return evenimentRepository.save(eveniment);
    }

    @Transactional(readOnly = true)
    public List<Eveniment> getEvenimenteByUtilizator(String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));
        return evenimentRepository.findByUtilizatorOrderByDataInceputDesc(utilizator);
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
        // câmpurile dataCreare și utilizator nu ar trebui actualizate aici
        // dataUltimaModificare este gestionată de @PreUpdate

        return evenimentRepository.save(evenimentExistent);
    }

    @Transactional
    public void deleteEveniment(Long id, String numeUtilizator) {
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(numeUtilizator)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul nu a fost găsit: " + numeUtilizator));

        Eveniment eveniment = evenimentRepository.findById(id)
                .filter(event -> event.getUtilizator().equals(utilizator))
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost găsit sau nu poate fi șters: " + id)); // TODO: Custom exception

        evenimentRepository.delete(eveniment);
    }
}
