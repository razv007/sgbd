package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.dto.UpdateUserDto;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import java.util.Optional;

public interface UserService {

    Utilizator updateUserProfile(Long userId, UpdateUserDto updateUserDto);
    Optional<Utilizator> findByNumeUtilizator(String numeUtilizator);
    // Alte metode necesare pentru gestionarea utilizatorilor pot fi adăugate aici
    // de exemplu: saveUser, updateUser, deleteUser, findAllUsers etc.
}
