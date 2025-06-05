package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.FriendRequestPayloadDto;
import com.arhiva_digitala.digital_archive_api.dto.PrietenieDto;
import com.arhiva_digitala.digital_archive_api.security.UserPrincipal;
import com.arhiva_digitala.digital_archive_api.service.PrietenieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/prietenii")
public class PrietenieController {

    private static final Logger logger = LoggerFactory.getLogger(PrietenieController.class);
    private final PrietenieService prietenieService;

    @Autowired
    public PrietenieController(PrietenieService prietenieService) {
        this.prietenieService = prietenieService;
    }

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrietenieDto> sendFriendRequest(@AuthenticationPrincipal UserPrincipal currentUser,
                                                          @Valid @RequestBody FriendRequestPayloadDto payload) {
        logger.info("Utilizatorul {} trimite cerere de prietenie către {}", currentUser.getUsername(), payload.getReceiverUsername());
        PrietenieDto prietenieDto = prietenieService.sendFriendRequest(currentUser.getUsername(), payload.getReceiverUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(prietenieDto);
    }

    @PostMapping("/{friendshipId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrietenieDto> acceptFriendRequest(@AuthenticationPrincipal UserPrincipal currentUser,
                                                            @PathVariable Long friendshipId) {
        logger.info("Utilizatorul {} acceptă cererea de prietenie ID {}", currentUser.getUsername(), friendshipId);
        PrietenieDto prietenieDto = prietenieService.acceptFriendRequest(friendshipId, currentUser.getUsername());
        return ResponseEntity.ok(prietenieDto);
    }

    @PostMapping("/{friendshipId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrietenieDto> rejectFriendRequest(@AuthenticationPrincipal UserPrincipal currentUser,
                                                            @PathVariable Long friendshipId) {
        logger.info("Utilizatorul {} respinge cererea de prietenie ID {}", currentUser.getUsername(), friendshipId);
        PrietenieDto prietenieDto = prietenieService.rejectFriendRequest(friendshipId, currentUser.getUsername());
        return ResponseEntity.ok(prietenieDto);
    }

    @DeleteMapping("/{friendshipId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelFriendRequest(@AuthenticationPrincipal UserPrincipal currentUser,
                                                      @PathVariable Long friendshipId) {
        logger.info("Utilizatorul {} anulează cererea de prietenie ID {}", currentUser.getUsername(), friendshipId);
        prietenieService.cancelFriendRequest(friendshipId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/unfriend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfriendUser(@AuthenticationPrincipal UserPrincipal currentUser,
                                             @RequestParam String friendUsername) {
        logger.info("Utilizatorul {} desface prietenia cu {}", currentUser.getUsername(), friendUsername);
        prietenieService.unfriend(currentUser.getUsername(), friendUsername);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrietenieDto>> getPendingReceivedRequests(@AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Utilizatorul {} cere lista de cereri de prietenie primite", currentUser.getUsername());
        List<PrietenieDto> requests = prietenieService.getPendingReceivedFriendRequests(currentUser.getUsername());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrietenieDto>> getPendingSentRequests(@AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Utilizatorul {} cere lista de cereri de prietenie trimise", currentUser.getUsername());
        List<PrietenieDto> requests = prietenieService.getPendingSentFriendRequests(currentUser.getUsername());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/friends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrietenieDto>> getFriends(@AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Utilizatorul {} cere lista de prieteni", currentUser.getUsername());
        List<PrietenieDto> friends = prietenieService.getFriends(currentUser.getUsername());
        return ResponseEntity.ok(friends);
    }
}
