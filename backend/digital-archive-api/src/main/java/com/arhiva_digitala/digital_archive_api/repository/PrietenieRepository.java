package com.arhiva_digitala.digital_archive_api.repository;

import com.arhiva_digitala.digital_archive_api.model.Prietenie;
import com.arhiva_digitala.digital_archive_api.model.StarePrietenie;
import com.arhiva_digitala.digital_archive_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrietenieRepository extends JpaRepository<Prietenie, Long> {

    // Găsește o relație de prietenie existentă între doi utilizatori, indiferent de ordine și stare.
    // Serviciul va trebui să verifice dacă user1 și user2 trebuie interschimbați pentru căutare dacă există o convenție de ordine în BD.
    // Pentru convenția user1=sender, user2=receiver, căutarea trebuie să fie bidirecțională.
    @Query("SELECT p FROM Prietenie p WHERE (p.utilizator1 = :userA AND p.utilizator2 = :userB) OR (p.utilizator1 = :userB AND p.utilizator2 = :userA)")
    Optional<Prietenie> findExistingRelationship(@Param("userA") User userA, @Param("userB") User userB);

    // Găsește cererile de prietenie primite de un utilizator (unde utilizatorul este utilizator2) și au o anumită stare (ex: IN_ASTEPTARE)
    List<Prietenie> findByUtilizator2AndStare(User utilizatorReceiver, StarePrietenie stare);

    // Găsește cererile de prietenie trimise de un utilizator (unde utilizatorul este utilizator1) și au o anumită stare (ex: IN_ASTEPTARE)
    List<Prietenie> findByUtilizator1AndStare(User utilizatorSender, StarePrietenie stare);

    // Găsește toate prieteniile ACCEPTATE ale unui utilizator (fie ca utilizator1, fie ca utilizator2)
    @Query("SELECT p FROM Prietenie p WHERE (p.utilizator1 = :user OR p.utilizator2 = :user) AND p.stare = com.arhiva_digitala.digital_archive_api.model.StarePrietenie.ACCEPTATA")
    List<Prietenie> findAcceptedFriendships(@Param("user") User user);

    // Găsește toate relațiile (indiferent de stare) în care un utilizator este implicat
    @Query("SELECT p FROM Prietenie p WHERE p.utilizator1 = :user OR p.utilizator2 = :user")
    List<Prietenie> findAllRelationshipsInvolvingUser(@Param("user") User user);

}
