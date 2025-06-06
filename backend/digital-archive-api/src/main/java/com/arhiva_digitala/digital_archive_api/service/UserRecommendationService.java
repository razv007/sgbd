package com.arhiva_digitala.digital_archive_api.service;

import com.arhiva_digitala.digital_archive_api.dto.UserDto;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.repository.UserRepository;
import com.arhiva_digitala.digital_archive_api.utils.FibonacciHeap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(UserRecommendationService.class);

    @Autowired
    private UserRepository userRepository;

    // Presupunem că avem un UserService pentru a prelua detalii complete despre utilizator
    // Dacă nu există, putem adăuga o metodă în UserRepository pentru a prelua Utilizator după ID
    @Autowired
    private UserService userService; // Asigură-te că UserService și implementarea sa există

    public List<UserDto> getRecommendations(Long currentUserId, int numberOfRecommendations) {
        logger.info("Generating recommendations for user ID: {}, numberOfRecommendations: {}", currentUserId, numberOfRecommendations);
        FibonacciHeap<Long> recommendationHeap = new FibonacciHeap<>();
        // Map pentru a stoca nodurile, util dacă vrem să facem decreaseKey ulterior
        // Map<Long, FibonacciHeap.Node<Long>> userNodeMap = new HashMap<>();

        List<Utilizator> allUsers = userRepository.findAll();
        logger.debug("Retrieved {} total users from database for potential recommendations.", allUsers.size());
        List<UserDto> recommendedUsers = new ArrayList<>();

        for (Utilizator potentialRecommendation : allUsers) {
            if (potentialRecommendation.getId().equals(currentUserId)) {
                logger.trace("Skipping current user ID: {}", currentUserId);
                continue; // Nu recomanda utilizatorul curent lui însuși
            }

            Double similarityScore = null;
            try {
                logger.trace("Calculating similarity between user {} and user {}", currentUserId, potentialRecommendation.getId());
                similarityScore = userRepository.calculateSimilarityScore(currentUserId, potentialRecommendation.getId());
            } catch (Exception e) {
                logger.error("Error calculating similarity score between user {} and user {}: {}",
                        currentUserId, potentialRecommendation.getId(), e.getMessage(), e);
                // Putem decide să continuăm fără acest utilizator sau să asignăm un scor default
            }


            if (similarityScore != null) {
                // Deoarece FibonacciHeap.extractMin() returnează cel mai MIC element,
                // iar noi vrem cel mai MARE scor de similaritate, inserăm cu prioritate negativă.
                // Un scor mai mare de similaritate => o valoare negativă mai mică (mai aproape de -infinit)
                // => va fi extras mai devreme.
                logger.debug("User ID: {}, Raw Similarity Score: {}, Heap Priority (negative score): {}",
                        potentialRecommendation.getId(), similarityScore, -similarityScore);
                recommendationHeap.insert(potentialRecommendation.getId(), -similarityScore);
                // userNodeMap.put(potentialRecommendation.getId(), node);
            } else {
                 logger.warn("Similarity score is null for user ID: {}. Skipping.", potentialRecommendation.getId());
            }
        }

        logger.debug("FibonacciHeap state before extraction: size = {}, isEmpty = {}", recommendationHeap.size(), recommendationHeap.isEmpty());
        int count = 0;
        while (!recommendationHeap.isEmpty() && count < numberOfRecommendations) {
            Long recommendedUserId = recommendationHeap.extractMin();
            if (recommendedUserId != null) {
                logger.trace("Extracted user ID {} from heap.", recommendedUserId);
                // Preluăm detalii complete despre utilizator pentru DTO
                // Folosim direct findById din UserRepository dacă UserService nu e configurat complet
                 Utilizator user = userRepository.findById(recommendedUserId).orElse(null);
                if (user != null) {
                    recommendedUsers.add(new UserDto(user.getId(), user.getNumeUtilizator()));
                    logger.debug("Added user ID {} ({}) to recommendations list.", recommendedUserId, user.getNumeUtilizator());
                } else {
                    logger.warn("Could not find user details for recommended ID: {}. User might have been deleted.", recommendedUserId);
                }
                count++;
            }
        }
        logger.info("Generated {} recommendations for user ID: {}. Recommended user IDs: {}",
                recommendedUsers.size(),
                currentUserId,
                recommendedUsers.stream().map(UserDto::getId).collect(Collectors.toList()));
        return recommendedUsers;
    }
}
