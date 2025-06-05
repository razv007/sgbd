package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.JwtResponse;
import com.arhiva_digitala.digital_archive_api.dto.LoginRequest;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.security.jwt.JwtUtils;
import com.arhiva_digitala.digital_archive_api.security.services.UserDetailsImpl;
import com.arhiva_digitala.digital_archive_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController // Marks this class as a REST controller, combining @Controller and @ResponseBody
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        System.out.println("########### AuthController dependency-injecting constructor called ###########");
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signup") // Maps HTTP POST requests to /api/auth/signup to this method
    public ResponseEntity<?> registerUser(@RequestBody Utilizator signUpRequest) {
        // @RequestBody maps the HTTP request body to the Utilizator object
        try {
            Utilizator registeredUser = authService.registerUser(signUpRequest);
            // You might want to return a DTO (Data Transfer Object) here
            // instead of the full Utilizator entity to avoid exposing sensitive data like password hash.
            // For now, we'll return a success message.
            return ResponseEntity.ok("User registered successfully! ID: " + registeredUser.getId());
        } catch (Exception e) {
            // Basic error handling. In a real app, you'd have more specific error responses.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNumeUtilizator(), loginRequest.getParola()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // String rol = userDetails.getAuthorities().stream()
        //        .map(GrantedAuthority::getAuthority)
        //        .findFirst()
        //        .orElse(null); // Assuming single role, adjust if multiple roles are possible
        // Simplification: Assuming getRol() exists on UserDetailsImpl or we get it from the Utilizator entity if needed
        // For now, let's assume the UserDetailsImpl has a direct way to get the role string if it was set during build.
        // If not, we would fetch the Utilizator entity again or ensure UserDetailsImpl stores it.
        // The JwtResponse constructor expects a role string.

        // Let's retrieve the role from UserDetailsImpl's authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        // Assuming the first role is the primary one, or adjust logic if multiple roles are handled differently
        String userRole = roles.isEmpty() ? null : roles.get(0);


        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userRole));
    }
}
