package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.dto.UserProfileDto;
import com.arhiva_digitala.digital_archive_api.model.User;

public interface UserService {
    User findByNumeUtilizator(String numeUtilizator);
    User updateUserProfile(String numeUtilizator, UserProfileDto userProfileDto);
    // Aici poți adăuga și alte metode de serviciu necesare pentru utilizatori
    // de exemplu: saveUser, updateUser, deleteUser, etc.
}
