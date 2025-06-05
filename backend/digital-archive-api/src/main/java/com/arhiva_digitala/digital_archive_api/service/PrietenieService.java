package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.dto.PrietenieDto;
import com.arhiva_digitala.digital_archive_api.model.Prietenie;

import java.util.List;

public interface PrietenieService {

    /**
     * Trimite o cerere de prietenie de la un utilizator la altul.
     * @param senderUsername Numele utilizatorului care trimite cererea.
     * @param receiverUsername Numele utilizatorului care primește cererea.
     * @return PrietenieDto reprezentând cererea creată.
     * @throws com.arhiva_digitala.digital_archive_api.exception.UserNotFoundException dacă unul dintre utilizatori nu este găsit.
     * @throws com.arhiva_digitala.digital_archive_api.exception.FriendshipRequestAlreadyExistsException dacă o cerere sau prietenie deja există.
     * @throws com.arhiva_digitala.digital_archive_api.exception.SelfFriendshipRequestException dacă utilizatorul încearcă să se împrietenească cu el însuși.
     */
    PrietenieDto sendFriendRequest(String senderUsername, String receiverUsername);

    /**
     * Acceptă o cerere de prietenie.
     * @param friendshipId ID-ul înregistrării de prietenie (cerere).
     * @param currentUserUsername Numele utilizatorului autentificat care acceptă cererea (trebuie să fie destinatarul cererii).
     * @return PrietenieDto reprezentând prietenia actualizată.
     * @throws com.arhiva_digitala.digital_archive_api.exception.FriendshipNotFoundException dacă cererea nu este găsită.
     * @throws com.arhiva_digitala.digital_archive_api.exception.UnauthorizedOperationException dacă utilizatorul curent nu este destinatarul cererii.
     * @throws com.arhiva_digitala.digital_archive_api.exception.InvalidFriendshipStateException dacă cererea nu este în starea IN_ASTEPTARE.
     */
    PrietenieDto acceptFriendRequest(Long friendshipId, String currentUserUsername);

    /**
     * Respinge o cerere de prietenie.
     * @param friendshipId ID-ul înregistrării de prietenie (cerere).
     * @param currentUserUsername Numele utilizatorului autentificat care respinge cererea (trebuie să fie destinatarul cererii).
     * @return PrietenieDto reprezentând prietenia actualizată.
     */
    PrietenieDto rejectFriendRequest(Long friendshipId, String currentUserUsername);

    /**
     * Anulează o cerere de prietenie trimisă.
     * @param friendshipId ID-ul înregistrării de prietenie (cerere).
     * @param currentUserUsername Numele utilizatorului autentificat care anulează cererea (trebuie să fie inițiatorul cererii).
     * @return PrietenieDto reprezentând prietenia actualizată (sau null dacă a fost ștearsă).
     */
    void cancelFriendRequest(Long friendshipId, String currentUserUsername);

    /**
     * Desface o prietenie existentă.
     * @param currentUserUsername Numele utilizatorului autentificat care inițiază acțiunea.
     * @param friendUsername Numele utilizatorului cu care se desface prietenia.
     */
    void unfriend(String currentUserUsername, String friendUsername);

    /**
     * Obține lista cererilor de prietenie primite și în așteptare pentru un utilizator.
     * @param username Numele utilizatorului.
     * @return Lista de PrietenieDto.
     */
    List<PrietenieDto> getPendingReceivedFriendRequests(String username);

    /**
     * Obține lista cererilor de prietenie trimise și în așteptare de către un utilizator.
     * @param username Numele utilizatorului.
     * @return Lista de PrietenieDto.
     */
    List<PrietenieDto> getPendingSentFriendRequests(String username);

    /**
     * Obține lista de prieteni acceptați pentru un utilizator.
     * @param username Numele utilizatorului.
     * @return Lista de PrietenieDto (sau un DTO mai simplu doar cu informațiile prietenului).
     */
    List<PrietenieDto> getFriends(String username); // Poate returna UserDto al prietenilor direct

    // TODO: Metode pentru blocare/deblocare utilizatori dacă este necesar
    // PrietenieDto blockUser(String currentUserUsername, String userToBlockUsername);
    // void unblockUser(String currentUserUsername, String userToUnblockUsername);
    // List<UserDto> getBlockedUsers(String username);
}
