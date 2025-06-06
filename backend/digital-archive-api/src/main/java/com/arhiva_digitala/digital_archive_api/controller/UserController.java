package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.CurrentUserDto;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.security.UserPrincipal;
import com.arhiva_digitala.digital_archive_api.service.UserService;
import com.arhiva_digitala.digital_archive_api.service.UserRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import com.arhiva_digitala.digital_archive_api.dto.UpdateUserDto;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.List;
import com.arhiva_digitala.digital_archive_api.dto.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;
import com.arhiva_digitala.digital_archive_api.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRecommendationService userRecommendationService;

    @Autowired
    public UserController(UserService userService, UserRecommendationService userRecommendationService) {
        this.userService = userService;
        this.userRecommendationService = userRecommendationService;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipalArgument) {
        logger.info("--- UserController#getCurrentUser --- START --- ");
        logger.debug("Received @AuthenticationPrincipal UserPrincipal userPrincipalArgument: {}", 
                     (userPrincipalArgument == null ? "null" : userPrincipalArgument.getClass().getName() + " with username: " + userPrincipalArgument.getUsername()));

        Authentication authenticationFromContext = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationFromContext == null) {
            logger.warn("Authentication object from SecurityContextHolder is NULL.");
        } else {
            logger.info("Authentication object from SecurityContextHolder: {} ", authenticationFromContext.getClass().getName());
            logger.info("Is Authenticated: {}", authenticationFromContext.isAuthenticated());
            Object principalFromContext = authenticationFromContext.getPrincipal();
            if (principalFromContext == null) {
                logger.warn("Principal from Authentication object is NULL.");
            } else {
                logger.info("Principal object from Authentication: {} ", principalFromContext.getClass().getName());
                if (principalFromContext instanceof UserPrincipal) {
                    UserPrincipal ucPrincipal = (UserPrincipal) principalFromContext;
                    logger.info("Principal IS an instance of UserPrincipal. Username: {}", ucPrincipal.getUsername());
                } else {
                    logger.warn("Principal IS NOT an instance of UserPrincipal. It is: {}", principalFromContext.getClass().getName());
                }
            }
        }

        // Use userPrincipalArgument for the logic as intended by @AuthenticationPrincipal
        UserPrincipal effectiveUserPrincipal = userPrincipalArgument; 
        // As a fallback for diagnostics, if userPrincipalArgument is null but context has it, we could try using it:
        // if (effectiveUserPrincipal == null && authenticationFromContext != null && authenticationFromContext.getPrincipal() instanceof UserPrincipal) {
        //    logger.warn("@AuthenticationPrincipal was null, attempting to use principal from SecurityContextHolder directly.");
        //    effectiveUserPrincipal = (UserPrincipal) authenticationFromContext.getPrincipal();
        // }

        logger.debug("Effective UserPrincipal for processing: {}", 
                     (effectiveUserPrincipal == null ? "null" : "Instance of " + effectiveUserPrincipal.getClass().getName()));

        if (effectiveUserPrincipal == null) {
            logger.warn("EffectiveUserPrincipal is null. Returning 401 Unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Log more details from userPrincipal if needed for debugging
        // logger.debug("UserPrincipal ID: {}, Username: {}, Email: {}, NumeComplet: {}, DataNastere: {}, Authorities: {}", 
        //        effectiveUserPrincipal.getId(), effectiveUserPrincipal.getUsername(), effectiveUserPrincipal.getEmail(), 
        //        effectiveUserPrincipal.getNumeComplet(), effectiveUserPrincipal.getDataNastere(), effectiveUserPrincipal.getAuthorities());

        String dataNastereStr = null;
        if (effectiveUserPrincipal.getDataNastere() != null) {
            dataNastereStr = effectiveUserPrincipal.getDataNastere().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
        }

        CurrentUserDto currentUserDto = new CurrentUserDto(
                effectiveUserPrincipal.getId(),
                effectiveUserPrincipal.getUsername(), // getNumeUtilizator() or getUsername() from UserPrincipal
                effectiveUserPrincipal.getEmail(),
                effectiveUserPrincipal.getNumeComplet(),
                dataNastereStr,
                effectiveUserPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
        logger.info("Successfully created CurrentUserDto for user: {}", effectiveUserPrincipal.getUsername());
        logger.info("--- UserController#getCurrentUser --- END --- ");
        return ResponseEntity.ok(currentUserDto);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal UserPrincipal currentUserPrincipal,
                                             @Valid @RequestBody UpdateUserDto updateUserDto) {
        logger.info("--- UserController#updateUserProfile --- START ---");
        if (currentUserPrincipal == null) {
            logger.warn("UserPrincipal is null in updateUserProfile. Returning 401 Unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator neautorizat.");
        }

        logger.info("Updating profile for user ID: {}", currentUserPrincipal.getId());

        try {
            Utilizator utilizatorActualizat = userService.updateUserProfile(currentUserPrincipal.getId(), updateUserDto);
            
            CurrentUserDto updatedCurrentUserDto = new CurrentUserDto(
                utilizatorActualizat.getId(),
                utilizatorActualizat.getNumeUtilizator(),
                utilizatorActualizat.getEmail(),
                utilizatorActualizat.getNumeComplet(),
                utilizatorActualizat.getDataNastere() != null ? utilizatorActualizat.getDataNastere().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
                currentUserPrincipal.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList())
            );
            logger.info("Profile updated successfully for user ID: {}. Returning updated profile.", currentUserPrincipal.getId());
            return ResponseEntity.ok(updatedCurrentUserDto);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found when trying to update profile for ID: {}. Details: {}", currentUserPrincipal.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid data provided for profile update for user ID: {}. Details: {}", currentUserPrincipal.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating profile for user ID: {}. Details: ", currentUserPrincipal.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare internă la actualizarea profilului.");
        }
    }

    @GetMapping("/recommendations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserRecommendations(@AuthenticationPrincipal UserPrincipal currentUserPrincipal,
                                                      @RequestParam(defaultValue = "5") int count) {
        logger.info("--- UserController#getUserRecommendations --- START --- User ID: {}, Count: {}", currentUserPrincipal.getId(), count);
        if (currentUserPrincipal == null) {
            logger.warn("UserPrincipal is null in getUserRecommendations. Returning 401 Unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator neautorizat.");
        }

        try {
            List<UserDto> recommendations = userRecommendationService.getRecommendations(currentUserPrincipal.getId(), count);
            if (recommendations.isEmpty()) {
                logger.info("No recommendations found for user ID: {}", currentUserPrincipal.getId());
                return ResponseEntity.noContent().build(); // 204 No Content
            }
            logger.info("Successfully retrieved {} recommendations for user ID: {}", recommendations.size(), currentUserPrincipal.getId());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error retrieving recommendations for user ID: {}. Details: ", currentUserPrincipal.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare internă la preluarea recomandărilor.");
        }
    }
}
