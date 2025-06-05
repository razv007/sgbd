package com.arhiva_digitala.digital_archive_api.security.services;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.repository.UtilizatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    UtilizatorRepository utilizatorRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        Utilizator utilizator = utilizatorRepository.findByNumeUtilizator(username)
                .orElseThrow(() -> {
            logger.error("User Not Found with username: {}", username);
            return new UsernameNotFoundException("User Not Found with username: " + username);
        });
        logger.info("User found: {}. Building UserDetails.", username);

        return UserDetailsImpl.build(utilizator);
    }
}
