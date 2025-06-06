package com.arhiva_digitala.digital_archive_api.security;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private static final Logger logger = LoggerFactory.getLogger(UserPrincipal.class);
    private static final long serialVersionUID = 1L;

    private Long id;
    private String numeUtilizator;
    private String email;
    @JsonIgnore
    private String parola;
    private Collection<? extends GrantedAuthority> authorities;
    private String numeComplet;
    private LocalDate dataNastere;

    public UserPrincipal(Long id, String numeUtilizator, String email, String parola, Collection<? extends GrantedAuthority> authorities, String numeComplet, LocalDate dataNastere) {
        this.id = id;
        this.numeUtilizator = numeUtilizator;
        this.email = email;
        this.parola = parola;
        this.authorities = authorities;
        this.numeComplet = numeComplet;
        this.dataNastere = dataNastere;
    }

    public static UserPrincipal create(Utilizator utilizator) {
        // TODO: Implement logic to get roles/authorities for the user if they exist
        // For now, using a default role or an empty list.
        // Example: List<GrantedAuthority> authorities = utilizator.getRoles().stream()
        // .map(role -> new SimpleGrantedAuthority(role.getName().name()))
        // .collect(Collectors.toList());
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // Placeholder

        logger.info("UserPrincipal.create() called for user: {}. Creating UserPrincipal instance.", utilizator.getNumeUtilizator());
        return new UserPrincipal(
                utilizator.getId(),
                utilizator.getNumeUtilizator(),
                utilizator.getEmail(),
                utilizator.getParola(),
                authorities,
                utilizator.getNumeComplet(),
                utilizator.getDataNastere()
        );
    }

    public Long getId() {
        return id;
    }

    public String getNumeUtilizator() {
        return numeUtilizator;
    }

    public String getEmail() {
        return email;
    }

    public String getNumeComplet() {
        return numeComplet;
    }

    public LocalDate getDataNastere() {
        return dataNastere;
    }

    @Override
    public String getUsername() {
        return numeUtilizator;
    }

    @Override
    public String getPassword() {
        return parola;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
