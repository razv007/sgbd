package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.dto.PrietenieDto;
import com.arhiva_digitala.digital_archive_api.dto.UserProfileDto;
import com.arhiva_digitala.digital_archive_api.exception.*;
import com.arhiva_digitala.digital_archive_api.model.Prietenie;
import com.arhiva_digitala.digital_archive_api.model.StarePrietenie;
import com.arhiva_digitala.digital_archive_api.model.User;
import com.arhiva_digitala.digital_archive_api.repository.PrietenieRepository;
import com.arhiva_digitala.digital_archive_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrietenieServiceImpl implements PrietenieService {

    private static final Logger logger = LoggerFactory.getLogger(PrietenieServiceImpl.class);

    private final PrietenieRepository prietenieRepository;
    private final UserRepository userRepository;
    // private final UserService userService; // Sau injectăm direct UserRepository

    @Autowired
    public PrietenieServiceImpl(PrietenieRepository prietenieRepository, UserRepository userRepository) {
        this.prietenieRepository = prietenieRepository;
        this.userRepository = userRepository;
    }

    // --- Mapper methods ---
    private UserProfileDto convertUserToUserProfileDto(User user) {
        if (user == null) return null;
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setNumeUtilizator(user.getNumeUtilizator());
        dto.setEmail(user.getEmail());
        dto.setNumeComplet(user.getNumeComplet());
        dto.setDataNasterii(user.getDataNastere() != null ? user.getDataNastere().toString() : null);
        // Nu setăm parola sau alte câmpuri sensibile
        return dto;
    }

    private PrietenieDto convertPrietenieToDto(Prietenie prietenie) {
        if (prietenie == null) return null;
        return new PrietenieDto(
                prietenie.getId(),
                convertUserToUserProfileDto(prietenie.getUtilizator1()),
                convertUserToUserProfileDto(prietenie.getUtilizator2()),
                prietenie.getStare(),
                prietenie.getDataSolicitare(),
                prietenie.getDataRaspuns()
        );
    }
    // --- End Mapper methods ---

    @Override
    @Transactional
    public PrietenieDto sendFriendRequest(String senderUsername, String receiverUsername) {
        if (senderUsername.equals(receiverUsername)) {
            throw new SelfFriendshipRequestException("Nu poți trimite o cerere de prietenie către tine însuți.");
        }

        User sender = userRepository.findByNumeUtilizator(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul trimițător nu a fost găsit: " + senderUsername));
        User receiver = userRepository.findByNumeUtilizator(receiverUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul destinatar nu a fost găsit: " + receiverUsername));

        // Verifică dacă există deja o relație (în orice stare)
        Optional<Prietenie> existingRelationship = prietenieRepository.findExistingRelationship(sender, receiver);
        if (existingRelationship.isPresent()) {
            Prietenie rel = existingRelationship.get();
            throw new FriendshipRequestAlreadyExistsException(
                    String.format("O relație între %s și %s deja există cu starea: %s.", senderUsername, receiverUsername, rel.getStare()));
        }

        Prietenie newRequest = new Prietenie(sender, receiver);
        newRequest.setStare(StarePrietenie.IN_ASTEPTARE);
        newRequest.setDataSolicitare(LocalDateTime.now());

        Prietenie savedRequest = prietenieRepository.save(newRequest);
        logger.info("Cerere de prietenie trimisă de la {} la {}. ID: {}", senderUsername, receiverUsername, savedRequest.getId());
        return convertPrietenieToDto(savedRequest);
    }

    @Override
    @Transactional
    public PrietenieDto acceptFriendRequest(Long friendshipId, String currentUserUsername) {
        User currentUser = userRepository.findByNumeUtilizator(currentUserUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUserUsername));

        Prietenie request = prietenieRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Cererea de prietenie cu ID " + friendshipId + " nu a fost găsită."));

        // Verifică dacă utilizatorul curent este destinatarul cererii (utilizator2)
        if (!request.getUtilizator2().equals(currentUser)) {
            throw new UnauthorizedOperationException("Nu ești autorizat să accepți această cerere de prietenie.");
        }

        if (request.getStare() != StarePrietenie.IN_ASTEPTARE) {
            throw new InvalidFriendshipStateException("Cererea de prietenie nu este în starea IN_ASTEPTARE. Stare actuală: " + request.getStare());
        }

        request.setStare(StarePrietenie.ACCEPTATA);
        request.setDataRaspuns(LocalDateTime.now());
        Prietenie updatedRequest = prietenieRepository.save(request);
        logger.info("Cererea de prietenie ID {} a fost ACCEPTATĂ de {}.", friendshipId, currentUserUsername);
        return convertPrietenieToDto(updatedRequest);
    }

    @Override
    @Transactional
    public PrietenieDto rejectFriendRequest(Long friendshipId, String currentUserUsername) {
        User currentUser = userRepository.findByNumeUtilizator(currentUserUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUserUsername));

        Prietenie request = prietenieRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Cererea de prietenie cu ID " + friendshipId + " nu a fost găsită."));

        // Utilizatorul curent trebuie să fie destinatarul (utilizator2) sau inițiatorul (utilizator1) pentru a respinge/anula
        // Dar pentru reject, e logic să fie destinatarul.
        if (!request.getUtilizator2().equals(currentUser)) {
            throw new UnauthorizedOperationException("Nu ești autorizat să respingi această cerere de prietenie.");
        }

        if (request.getStare() != StarePrietenie.IN_ASTEPTARE) {
            throw new InvalidFriendshipStateException("Cererea de prietenie nu poate fi respinsă. Stare actuală: " + request.getStare());
        }

        request.setStare(StarePrietenie.RESPINSA);
        request.setDataRaspuns(LocalDateTime.now());
        Prietenie updatedRequest = prietenieRepository.save(request);
        logger.info("Cererea de prietenie ID {} a fost RESPINSĂ de {}.", friendshipId, currentUserUsername);
        return convertPrietenieToDto(updatedRequest);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long friendshipId, String currentUserUsername) {
        User currentUser = userRepository.findByNumeUtilizator(currentUserUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUserUsername));

        Prietenie request = prietenieRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Cererea de prietenie cu ID " + friendshipId + " nu a fost găsită."));

        // Utilizatorul curent trebuie să fie inițiatorul cererii (utilizator1)
        if (!request.getUtilizator1().equals(currentUser)) {
            throw new UnauthorizedOperationException("Nu ești autorizat să anulezi această cerere de prietenie.");
        }

        if (request.getStare() != StarePrietenie.IN_ASTEPTARE) {
            throw new InvalidFriendshipStateException("Cererea de prietenie nu poate fi anulată. Stare actuală: " + request.getStare());
        }

        // Anularea înseamnă ștergerea cererii
        prietenieRepository.delete(request);
        logger.info("Cererea de prietenie ID {} a fost ANULATĂ de {}.", friendshipId, currentUserUsername);
    }

    @Override
    @Transactional
    public void unfriend(String currentUserUsername, String friendUsername) {
        User currentUser = userRepository.findByNumeUtilizator(currentUserUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUserUsername));
        User friendUser = userRepository.findByNumeUtilizator(friendUsername)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul prieten nu a fost găsit: " + friendUsername));

        Prietenie friendship = prietenieRepository.findExistingRelationship(currentUser, friendUser)
                .orElseThrow(() -> new FriendshipNotFoundException("Nu există o prietenie între " + currentUserUsername + " și " + friendUsername));

        if (friendship.getStare() != StarePrietenie.ACCEPTATA) {
            throw new InvalidFriendshipStateException("Nu se poate desface o prietenie care nu este în starea ACCEPTATA. Stare actuală: " + friendship.getStare());
        }

        // Desfacerea prieteniei înseamnă ștergerea înregistrării
        // Alternativ, s-ar putea marca starea ca 'TERMINATA' sau ceva similar dacă se dorește păstrarea istoricului.
        prietenieRepository.delete(friendship);
        logger.info("Prietenia dintre {} și {} (ID {}) a fost DESFĂCUTĂ.", currentUserUsername, friendUsername, friendship.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrietenieDto> getPendingReceivedFriendRequests(String username) {
        User user = userRepository.findByNumeUtilizator(username)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul nu a fost găsit: " + username));
        return prietenieRepository.findByUtilizator2AndStare(user, StarePrietenie.IN_ASTEPTARE)
                .stream()
                .map(this::convertPrietenieToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrietenieDto> getPendingSentFriendRequests(String username) {
        User user = userRepository.findByNumeUtilizator(username)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul nu a fost găsit: " + username));
        return prietenieRepository.findByUtilizator1AndStare(user, StarePrietenie.IN_ASTEPTARE)
                .stream()
                .map(this::convertPrietenieToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrietenieDto> getFriends(String username) {
        User user = userRepository.findByNumeUtilizator(username)
                .orElseThrow(() -> new UserNotFoundException("Utilizatorul nu a fost găsit: " + username));
        return prietenieRepository.findAcceptedFriendships(user)
                .stream()
                .map(this::convertPrietenieToDto)
                // Opțional: transformă în UserProfileDto al prietenului, nu întreaga PrietenieDto
                // .map(prietenieDto -> {
                //    // Identifică care dintre utilizator1 sau utilizator2 este prietenul efectiv
                //    if (Objects.equals(prietenieDto.getUtilizator1().getNumeUtilizator(), username)) {
                //        return prietenieDto.getUtilizator2();
                //    } else {
                //        return prietenieDto.getUtilizator1();
                //    }
                // })
                .collect(Collectors.toList());
    }
}
