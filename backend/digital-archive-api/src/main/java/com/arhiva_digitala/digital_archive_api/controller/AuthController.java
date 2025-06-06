package com.arhiva_digitala.digital_archive_api.controller;

import com.arhiva_digitala.digital_archive_api.dto.JwtResponse;
import com.arhiva_digitala.digital_archive_api.dto.LoginRequest;
import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.arhiva_digitala.digital_archive_api.security.jwt.JwtUtils;
import com.arhiva_digitala.digital_archive_api.security.UserPrincipal;
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

import java.time.LocalDate;
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

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody Utilizator signUpRequest) {
        try {
            Utilizator registeredUser = authService.registerUser(signUpRequest);
            return ResponseEntity.ok("User registered successfully! ID: " + registeredUser.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNumeUtilizator(), loginRequest.getParola()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Retrieve roles from UserPrincipal's authorities
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        // Assuming the first role is the primary one, or adjust logic if multiple roles are handled differently
        String userRole = roles.isEmpty() ? null : roles.get(0);

        // dataNastere is directly available in UserPrincipal
        LocalDate dataNastere = userPrincipal.getDataNastere(); // Ensure UserPrincipal has this method and it returns LocalDate
        String numeComplet = userPrincipal.getNumeComplet(); // Assuming UserPrincipal has getNumeComplet()

        return ResponseEntity.ok(new JwtResponse(jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(), // This is UserDetails.getUsername(), maps to UserPrincipal.numeUtilizator
                userPrincipal.getEmail(),
                userRole,
                dataNastere,
                numeComplet));
    }
}
